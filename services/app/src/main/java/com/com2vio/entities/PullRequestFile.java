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

@Entity
@Table(name = "pull_request_file")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestFile {

    @Id
    private String uuid;
    private String sha;
    private String filename;
    @Column(name = "previous_filename")
    private String previousFilename;
    private String status;
    private Integer additions;
    private Integer deletions;
    private Integer changes;
    @Column(name = "blob_url")
    private String blobUrl;
    @Column(name = "raw_url")
    private String rawUrl;
    @Column(name = "contents_url")
    private String contentsUrl;
    private String owner;
    private String repo;
    private Integer pull;
}
