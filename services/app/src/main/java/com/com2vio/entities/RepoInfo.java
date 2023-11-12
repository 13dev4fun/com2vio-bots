package com.com2vio.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "repo_info")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoInfo {

    @Id
    private String uuid;
    private String repo;
    private String owner;
    private String base;
    @Column(name = "pull_requests")
    private Boolean pullRequests;
    private Boolean comments;
    private Boolean commits;
    private Boolean files;
    @Column(name = "commit_comments")
    private Boolean commitComments;
    @Column(name = "files_downloaded")
    private Boolean filesDownloaded;
    private Boolean matched;
    private Boolean labelled;
    @Column(name = "post_processed")
    private Boolean postProcessed;
}
