package com.com2vio.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pull_request_commit_file_download")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileDownloadStatus {

    @Id
    private Long id;
    private String filename;
    private String version;
    private String url;
    // 0 for no, 1 for success, 2 for failed
    private Byte downloaded;
    private String commit;
    private Integer pull;
    private String owner;
    private String repo;
}
