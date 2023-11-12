package com.com2vio.controllers;

import com.com2vio.entities.PullRequest;
import com.com2vio.entities.ViolationCommentMatch;
import com.com2vio.repositories.PullRequestRepository;
import com.com2vio.repositories.ViolationCommentMatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PullRequestController {
    @Autowired
    private PullRequestRepository pullRequestRepository;

    @Autowired
    private ViolationCommentMatchRepository violationCommentMatchRepository;

    @GetMapping("/pulls/{owner}/{repo}")
    public Page<PullRequest> getPulls(@PathVariable("owner") String owner,
                                      @PathVariable("repo") String repo,
                                      @RequestParam("page") int page) {
        Pageable pageable = PageRequest.of(page, 15, Sort.by("number").descending());
        return pullRequestRepository.findAllByOwnerAndRepo(owner, repo, pageable);
    }

    @GetMapping("/matches/{owner}/{repo}")
    public Page<ViolationCommentMatch> getMatches(@PathVariable("owner") String owner,
                                      @PathVariable("repo") String repo,
                                      @RequestParam("page") int page) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by("pull").descending());
        return violationCommentMatchRepository.findAllByOwnerAndRepoOrderByLabelDesc(owner, repo, pageable);
    }
}
