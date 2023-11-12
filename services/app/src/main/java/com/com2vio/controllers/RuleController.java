package com.com2vio.controllers;

import com.com2vio.entities.RepoInfo;
import com.com2vio.services.RecommendService;
import com.com2vio.services.crawler.RepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RuleController {

    @Autowired
    private RepoService repoService;

    @Autowired
    private RecommendService recommendService;

    @GetMapping("/rules")
    public ResponseEntity<Object> getRecommendRules(@RequestParam(name = "owner") String owner,
                                                    @RequestParam(name = "repo") String repo,
                                                    @RequestParam(name = "size", defaultValue = "30") int size) {
        RepoInfo repoInfo = this.repoService.getRepoByOwnerAndRepo(owner, repo);
        if (Boolean.TRUE.equals(repoInfo.getPostProcessed())) {
            return ResponseEntity.ok(recommendService.getRecommendedRules(owner, repo, size));
        }
        return ResponseEntity.badRequest().body("Analyzing for repo: " + repo + " is in progress, please try it later");
    }

    @GetMapping("/results")
    public ResponseEntity<Object> getResults(@RequestParam(name = "owner") String owner,
                                             @RequestParam(name = "repo") String repo,
                                             @RequestParam(name = "size", defaultValue = "10") int size,
                                             @RequestParam(name = "page", defaultValue = "1") int page) {
        return ResponseEntity.ok(recommendService.getRuleResults(owner, repo, size, page));
    }
}
