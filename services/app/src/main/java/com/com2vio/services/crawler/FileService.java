package com.com2vio.services.crawler;

import com.com2vio.entities.PullRequest;
import com.com2vio.entities.PullRequestFile;
import com.com2vio.entities.RepoInfo;
import com.com2vio.repositories.FileRepository;
import com.com2vio.utils.LocalDateTimeTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
@Slf4j
public class FileService {

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
        .create();
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private OkHttpClient okHttpClient;
    @Autowired
    private FileRepository fileRepository;
    @Value("${github.token}")
    private String token;
    @Value("${github.endpoint}")
    private String endpoint;

    @KafkaListener(topics = "pull", groupId = "file-crawler")
    void listener(String pull) {
        PullRequest pr = gson.fromJson(pull, PullRequest.class);
        if (pr.getNumber() == -1) {
            RepoInfo repoInfo = new RepoInfo();
            repoInfo.setOwner(pr.getOwner());
            repoInfo.setRepo((pr.getRepo()));
            repoInfo.setFiles(true);
            kafkaTemplate.send("status", gson.toJson(repoInfo));
        } else {
            fetchAllFiles(pr, 1);
        }
    }

    private void fetchAllFiles(PullRequest pr, int page) {
        try {
            String url = String.format("%s/repos/%s/%s/pulls/%d/files?page=%d&per_page=100", endpoint, pr.getOwner(), pr.getRepo(), pr.getNumber(), page);
            Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build();
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body().string();
            JSONArray fileJsons = new JSONArray(responseBody);
            if (fileJsons.length() > 0) {
                for (int i = 0; i < fileJsons.length(); i++) {
                    try {
                        JSONObject fileJson = fileJsons.getJSONObject(i);
                        PullRequestFile pullRequestFile = extractFile(fileJson);
                        pullRequestFile.setRepo(pr.getRepo());
                        pullRequestFile.setOwner(pr.getOwner());
                        pullRequestFile.setUuid(String.valueOf(UUID.randomUUID()));
                        pullRequestFile.setPull(pr.getNumber());
                        fileRepository.insertIgnore(pullRequestFile);
                    } catch (Exception e) {
                        log.error(String.format("File extract failed: %s/%s #%d", pr.getOwner(), pr.getRepo(), pr.getNumber()));
                        e.printStackTrace();
                        // TODO retry fetching file info
                    }
                }
                // TODO parameter type error for inserting all
                page++;
                Thread.sleep(720); // sleep for 720ms between API requests
                if (fileJsons.length() >= 100) {
                    fetchAllFiles(pr, page);
                } else {
                    log.info(String.format("Files Done: %s/%s/%d.", pr.getOwner(), pr.getRepo(), pr.getNumber()));
                }
            } else {
                log.info(String.format("Files Done: %s/%s/%d.", pr.getOwner(), pr.getRepo(), pr.getNumber()));
            }
        } catch (Exception e) {
            log.error(String.format("Files error: %s/%s/%d.", pr.getOwner(), pr.getRepo(), pr.getNumber()));
            log.error(e.toString());
        }
    }

    private PullRequestFile extractFile(JSONObject fileJson) throws JSONException {
        PullRequestFile pullRequestFile = new PullRequestFile();
        pullRequestFile.setSha(fileJson.getString("sha"));
        pullRequestFile.setFilename(fileJson.getString("filename"));
        pullRequestFile.setPreviousFilename(fileJson.optString("previous_filename", null));
        pullRequestFile.setStatus(fileJson.getString("status"));
        pullRequestFile.setAdditions(fileJson.optInt("additions", 0));
        pullRequestFile.setDeletions(fileJson.optInt("deletions", 0));
        pullRequestFile.setChanges(fileJson.optInt("changes", 0));
        pullRequestFile.setBlobUrl(fileJson.getString("blob_url"));
        pullRequestFile.setRawUrl(fileJson.getString("raw_url"));
        pullRequestFile.setContentsUrl(fileJson.getString("contents_url"));
        return pullRequestFile;
    }

}
