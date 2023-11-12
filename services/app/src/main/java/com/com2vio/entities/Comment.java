package com.com2vio.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pull_request_comment")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {

    @Id
    private String uuid;
    private Integer id;
    @Column(name = "node_id")
    private String nodeId;
    @Column(name = "diff_hunk")
    private String diffHunk;
    private String path;
    @Column(name = "commit_id")
    private String commitId;
    @Column(name = "original_commit_id")
    private String originalCommitId;
    private String user;
    private String body;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "start_line")
    private Integer startLine;
    @Column(name = "original_start_line")
    private Integer originalStartLine;
    @Column(name = "start_side")
    private String startSide;
    private Integer line;
    @Column(name = "original_line")
    private Integer originalLine;
    private String side;
    private String owner;
    private String repo;
    private Integer pull;
    @Column(name = "pull_request_review_id")
    private Integer pullRequestReviewId;
}
