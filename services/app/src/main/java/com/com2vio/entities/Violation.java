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
@Table(name = "violation")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Violation {
    @Id
    private Long id;
    private String owner;
    private String repo;
    @Column(name = "pr")
    private String pull;
    @Column(name = "commit_id")
    private String commitId;
    @Column(name = "code_source")
    private String codeSource;
    @Column(name = "file_path")
    private String filePath;
    private String type;
    private String line;
    @Column(name = "match_status")
    private String matchStatus;
    private String detail;
    @Column(name = "violation_uuid")
    private String violationUUID;
    @Column(name = "matched_violation_uuid")
    private String matchedViolationUUID;
    @Column(name = "solve_way")
    private String solveWay;
    private String category;
}
