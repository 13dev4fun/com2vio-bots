package com.com2vio.controllers;

import static com.com2vio.utils.DataUtils.LOCAL_GSON;
import com.com2vio.entities.RepoInfo;
import com.com2vio.repositories.RepoInfoRepository;
import com.com2vio.services.crawler.CommitCommentService;
import com.com2vio.services.crawler.RepoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.transaction.Transactional;

@Slf4j
@RestController
public class ActionController {
    @Autowired
    private RepoService repoService;

    @Autowired
    private RepoInfoRepository repoInfoRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private CommitCommentService commitCommentService;

    @PostMapping("/download")
    public ResponseEntity<Object> doFileDownloading(@RequestBody RepoInfo repoInfo) {
        RepoInfo currentInfo = repoService.getRepoByOwnerAndRepo(repoInfo.getOwner(), repoInfo.getRepo());
        if (Boolean.TRUE.equals(currentInfo.getCommitComments())
            && !Boolean.TRUE.equals(currentInfo.getFilesDownloaded())) {
            String msg = LOCAL_GSON.toJson(repoInfo);
            kafkaTemplate.send("file", msg);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/commitComment")
    @Transactional
    public ResponseEntity<Object> doCommitCommentMatching(@RequestBody RepoInfo repoInfo) {
        // filter out comments that belongs to a commit in a pr's commit list
        // sometimes due to force push, a commit might not exist in a pr's final commit history
        // we do not take comments on such commits into consideration
        RepoInfo currentInfo = repoService.getRepoByOwnerAndRepo(repoInfo.getOwner(), repoInfo.getRepo());
        if (currentInfo != null && Boolean.TRUE.equals(currentInfo.getComments()) && Boolean.TRUE.equals(currentInfo.getCommits())
            && !Boolean.TRUE.equals(currentInfo.getCommitComments())) {
            try {
                commitCommentService.insertCommitComment(currentInfo.getOwner(), currentInfo.getRepo());
                currentInfo.setCommitComments(true);
                repoInfoRepository.updateRepoInfo(currentInfo);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                log.error(e.getMessage());
                return ResponseEntity.internalServerError().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/scan")
    public ResponseEntity<Object> doSonarScan(@RequestBody RepoInfo repoInfo) {
        // TODO trigger SonarQube and ViolationTracker to get violations
        return ResponseEntity.status(HttpStatusCode.valueOf(501)).build();
    }

    @PostMapping("/match")
    public ResponseEntity<Object> doMatching(@RequestBody RepoInfo repoInfo) {
        RepoInfo currentInfo = repoService.getRepoByOwnerAndRepo(repoInfo.getOwner(), repoInfo.getRepo());
        if (!Boolean.TRUE.equals(currentInfo.getMatched())) {
            String msg = LOCAL_GSON.toJson(repoInfo);
            kafkaTemplate.send("match-event", msg);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/post-processing")
    public ResponseEntity<Object> doPostprocessing(@RequestBody RepoInfo repoInfo) {
        RepoInfo currentInfo = repoService.getRepoByOwnerAndRepo(repoInfo.getOwner(), repoInfo.getRepo());
        if (!Boolean.TRUE.equals(currentInfo.getPostProcessed())) {
            String msg = LOCAL_GSON.toJson(repoInfo);
            kafkaTemplate.send("postprocessing-event", msg);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
