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
@Table(name = "pull_request")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequest {

    @Id
    private String uuid;
    private Long id;
    private Integer number;
    private String state;
    private Boolean locked;
    private String title;
    private String body;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
    @Column(name = "merged_at")
    private LocalDateTime mergedAt;
    @Column(name = "merge_commit_sha", length = 40)
    private String mergeCommitSha;
    @Column(name = "final_commit", length = 40)
    private String finalCommit;
    private Boolean draft;
    private String head;
    private String base;
    private String repo;
    private String owner;
    @Column(name = "node_id", length = 128)
    private String nodeId;
    private String user;
    @Column(name = "file_count")
    private Long fileCount;
    @Column(name = "comment_count")
    private Long commentCount;
    @Column(name = "commit_count")
    private Long commitCount;
}
