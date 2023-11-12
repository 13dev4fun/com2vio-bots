package com.com2vio.services;

import static com.com2vio.utils.DataUtils.LOCAL_GSON;
import com.com2vio.entities.RepoInfo;
import com.com2vio.repositories.RepoInfoRepository;
import com.com2vio.services.crawler.CommitCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StatusService {

    @Autowired
    private RepoInfoRepository repoInfoRepository;
    @Autowired
    private CommitCommentService commitCommentService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    @KafkaListener(topics = "status", groupId = "status")
    void updateRepoInfo(String info) {
        RepoInfo repoInfo = LOCAL_GSON.fromJson(info, RepoInfo.class);
        repoInfoRepository.updateRepoInfo(repoInfo);
        RepoInfo currentInfo = repoInfoRepository.findByOwnerAndRepo(repoInfo.getOwner(), repoInfo.getRepo());
        // TODO use message to trigger each step automatically
        if (currentInfo != null) {
//            if (Boolean.TRUE.equals(currentInfo.getComments()) && Boolean.TRUE.equals(currentInfo.getCommits())) {
//                if (!Boolean.TRUE.equals(currentInfo.getCommitComments())) {
//                    commitCommentService.insertCommitComment(currentInfo.getOwner(), currentInfo.getRepo());
//                    currentInfo.setCommitComments(true);
//                    repoInfoRepository.updateRepoInfo(currentInfo);
//                }
//            }
//            // start downloading files when commits, comments and files information are ready
//            if (Boolean.TRUE.equals(currentInfo.getCommitComments()) && Boolean.TRUE.equals(currentInfo.getFiles())) {
//                // comments, commits, files and commit_comment are all done for given repo
//                if (!Boolean.TRUE.equals(currentInfo.getFilesDownloaded())) {
//                    // start downloading files for this repo
//                    kafkaTemplate.send("file", LOCAL_GSON.toJson(currentInfo));
//                } else {
//                    // raw data are all collected
//                    // TODO start ViolationTracker for analyzing
//                }
//            }
        } else {
            // handle the case where no repoInfo is found
            log.error("Failed to find repo %s/%s", repoInfo.getOwner(), repoInfo.getRepo());
        }
    }
}
