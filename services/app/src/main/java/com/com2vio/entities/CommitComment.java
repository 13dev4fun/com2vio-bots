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
@Table(name = "commit_comment")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommitComment {

    @Id
    private Integer id;
    private Integer startLine;
    private Integer endLine;
    private String content;
    private String commitId;
    private String owner;
    private String repo;
    private Integer pull;
    @Column(name = "comment_uuid")
    private String commentUUID;
    private String file;
}
