package com.com2vio.controllers;

import com.com2vio.entities.RepoInfo;
import com.com2vio.services.crawler.RepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class RepoController {
    @Autowired
    private RepoService repoService;

    @PostMapping("/repo")
    public RepoInfo analyzeRepo(@RequestBody RepoInfo repoInfo) {
        return repoService.saveRepo(repoInfo);
    }

    @GetMapping("/repos")
    public List<RepoInfo> getRepos() {
        return repoService.getRepos();
    }
}
