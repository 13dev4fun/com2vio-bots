package com.com2vio.repositories;

import com.com2vio.entities.FileDownloadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import jakarta.transaction.Transactional;

@Repository
public interface FileDownloadStatusRepository extends JpaRepository<FileDownloadStatus, Long> {

    List<FileDownloadStatus> findByOwnerAndRepoAndPullAndCommitAndVersionAndFilename(
        String owner, String repo, Integer pull, String commit, String version, String filename);


    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO pull_request_commit_file_download " +
        "(owner, repo, pull, commit, filename, url, version) " +
        "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)",
        nativeQuery = true)
    void insertIgnore(String owner, String repo, int pull, String commit, String filename, String url, String version);

    @Modifying
    @Transactional
    @Query("UPDATE FileDownloadStatus p " +
        "SET p.downloaded = ?7 " +
        "WHERE p.owner = ?1 AND p.repo = ?2 AND p.pull = ?3 " +
        "AND p.commit = ?4 AND p.version = ?5 AND p.filename = ?6")
    void updateDownloaded(String owner, String repo, int pull, String commit, String version, String filename, Byte downloaded);
}
