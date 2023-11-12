package com.com2vio.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "violation_comment_match")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViolationCommentMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String owner;
    private String repo;
    private String pull;
    @Column(name = "commit_id")
    private String commitId;
    @Column(name = "file_path")
    private String filePath;
    @Column(name = "violation_uuid")
    private String violationUUID;
    private String type;
    private String detail;
    private String line;
    @Column(name = "comment_uuid")
    private String commentUUID;
    @Column(name = "start_line")
    private Integer startLine;
    @Column(name = "end_line")
    private Integer endLine;
    private String comment;
    @Column(name = "vio_code")
    private String vioCode;
    @Column(name = "cmt_code")
    private String cmtCode;
    private Integer offset;
    private Boolean label;
}
