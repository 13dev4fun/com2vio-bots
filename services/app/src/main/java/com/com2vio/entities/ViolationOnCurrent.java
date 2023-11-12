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
@Table(name = "violation_on_current")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViolationOnCurrent {
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
    private String detail;
    @Column(name = "violation_uuid")
    private String violationUUID;
}
