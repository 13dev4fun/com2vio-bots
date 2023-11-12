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
@Table(name = "pull_request_commit")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Commit {

    @Id
    private String uuid;
    @Column(name = "sha", length = 40)
    private String sha;
    @Column(name = "node_id", length = 128)
    private String nodeId;
    @Column(name = "url", length = 512)
    private String url;
    private String commit;
    private String author;
    private String committer;
    private String parents;
    private String owner;
    private String repo;
    private Integer pull;
    private Integer idx;
}
