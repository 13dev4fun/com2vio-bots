package com.com2vio.services.crawler;

import static com.com2vio.utils.DataUtils.LOCAL_GSON;
import com.com2vio.entities.CommitFileInfo;
import com.com2vio.entities.FileDownloadStatus;
import com.com2vio.entities.PullRequest;
import com.com2vio.entities.PullRequestFile;
import com.com2vio.entities.RepoInfo;
import com.com2vio.repositories.CommitCommentRepository;
import com.com2vio.repositories.FileDownloadStatusRepository;
import com.com2vio.repositories.FileRepository;
import com.com2vio.repositories.PullRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Service
@Slf4j
public class FileDownloadService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private final OkHttpClient httpClient = new OkHttpClient();
    @Autowired
    private PullRequestRepository pullRequestRepository;
    @Autowired
    private CommitCommentRepository commitCommentRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FileDownloadStatusRepository fileDownloadStatusRepository;

    @Value("${github.token}")
    private String token;
    @Value("${github.host}")
    private String host;
    @Value("${repos.dir}")
    private String reposDir;

    @KafkaListener(topics = "file", groupId = "file-downloader")
    void listener(String message) {
        RepoInfo repoInfo = LOCAL_GSON.fromJson(message, RepoInfo.class);
        List<PullRequest> prs = pullRequestRepository.findPullRequestWithComments(repoInfo.getOwner(), repoInfo.getRepo());
        if (prs != null) {
            prs.forEach(pr -> {
                List<CommitFileInfo> commitFileInfos = commitCommentRepository.findCommitFileInfo(pr.getOwner(), pr.getRepo(), pr.getNumber());
                List<PullRequestFile> pullRequestFiles = fileRepository.findJavaFilesByRepoAndPull(pr.getOwner(), pr.getRepo(), pr.getNumber());
                Map<String, Map<String, Object>> fileChangesInPR = new HashMap<>();
                for (PullRequestFile pullRequestFile : pullRequestFiles) {
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("status", pullRequestFile.getStatus());
                    fileData.put("previousFilename", pullRequestFile.getPreviousFilename());
                    fileChangesInPR.put(pullRequestFile.getFilename(), fileData);
                }
                for (CommitFileInfo row : commitFileInfos) {
                    Map<String, Object> change = fileChangesInPR.get(row.getFile());
                    if (change != null) {
                        try {
                            // download current
                            downloadFile(pr.getOwner(), pr.getRepo(), pr.getNumber(), row.getCommitId(), row.getFile(), "current");
                            // download accept
                            downloadFile(pr.getOwner(), pr.getRepo(), pr.getNumber(), row.getCommitId(), row.getFile(), "accepted", pr.getFinalCommit());
                            // download base
                            if (change.get("status").equals("modified") || change.get("status").equals("changed")) {
                                downloadFile(pr.getOwner(), pr.getRepo(), pr.getNumber(), row.getCommitId(), row.getFile(), "base", repoInfo.getBase());
                            } else if (change.get("status").equals("renamed")) {
                                downloadFile(pr.getOwner(), pr.getRepo(), pr.getNumber(), row.getCommitId(), (String) change.get("previousFilename"), "base", repoInfo.getBase());
                            } else if (change.get("status").equals("removed")) {
                                downloadFile(pr.getOwner(), pr.getRepo(), pr.getNumber(), row.getCommitId(), row.getFile(), "base", repoInfo.getBase());
                            } else {
                                // Do nothing for "added", "unchanged", "copied" files
                            }
                        } catch (Exception e) {
                            log.error("Failed to download changes for file: {}/{}/{} {}", pr.getOwner(), pr.getRepo(), pr.getNumber(), row.getFile());
                        }
                    } else {
                        // one case is when a file is added in a commit but got renamed
                        // in the final commit the file with its old name won't
                        // appear in the PR's file list, we ignore such changes for now
                        log.warn("File not found: {}/{}/{} {} {}", pr.getOwner(), pr.getRepo(), pr.getNumber(), row.getCommitId(), row.getFile());
                    }
                }
            });
        }
        // update file downloading status
        repoInfo.setFilesDownloaded(true);
        kafkaTemplate.send("status", LOCAL_GSON.toJson(repoInfo));
    }

    private void downloadFile(String owner, String repo, Integer pull, String commit, String file, String version) throws IOException {
        downloadFile(owner, repo, pull, commit, file, version, null);
    }

    public void downloadFile(String owner, String repo, int pull, String commit, String filename, String version, String commitToDownload) throws IOException {
        final String uid = String.format("%s/%s/%s/%s/%s", repo, pull, commit, version, filename);
        commitToDownload = commitToDownload != null ? commitToDownload : commit;
//        final String url = String.format("%s/%s/%s/raw/%s/%s", host, owner, repo, commitToDownload, filename);
        final String url = String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", owner, repo, commitToDownload, filename);

        try {
            final boolean alreadyDownloaded = checkIfFileDownloaded(owner, repo, pull, commit, version, filename);
            if (alreadyDownloaded) {
                log.info("Already exists:\t{}", uid);
                return;
            }
        } catch (Exception e) {
            log.warn("Failed to read file download status:\t{}\n{}", uid, e.getMessage());
        }

        // Insert into database for backup
        fileDownloadStatusRepository.insertIgnore(owner, repo, pull, commit, filename, url, version);

        final Request request = new Request.Builder()
            .url(url)
            .build();

        final Response response = httpClient.newCall(request).execute();
        try (ResponseBody body = response.body()) {
            if (response.isSuccessful() && body != null) {
                final File file = new File(reposDir, uid);
                file.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(body.bytes());
                    fileDownloadStatusRepository.updateDownloaded(owner, repo, pull, commit, version, filename, (byte)1);
                    log.info("File downloaded:\t{}", uid);
                }
            } else {
                if (response.code() == 404) {
                    fileDownloadStatusRepository.updateDownloaded(owner, repo, pull, commit, version, filename, (byte)2);
                }
                log.error("Failed to download file from URL:\t{}\t{}", url, response.code());
            }
        } catch (Exception e) {
            log.error("Failed to download file from URL: \t{}\n{}", url, e.getMessage());
        }
    }

    private boolean checkIfFileDownloaded(String owner, String repo, Integer pull, String commit, String version, String filename) {
        List<FileDownloadStatus> result = fileDownloadStatusRepository.findByOwnerAndRepoAndPullAndCommitAndVersionAndFilename(owner, repo, pull, commit, version, filename);
        return !result.isEmpty() || result.stream().filter(file -> file.getDownloaded() != null).count() > 1;
    }
}
