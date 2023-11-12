package com.com2vio.repositories;

import com.com2vio.entities.PullRequestFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import jakarta.transaction.Transactional;

@Repository
public interface FileRepository extends JpaRepository<PullRequestFile, String> {

    @Transactional
    @Modifying
    @Query(value = "INSERT IGNORE INTO pull_request_file (uuid, sha, filename, previous_filename, status, additions, deletions, changes, blob_url, raw_url, contents_url, owner, repo, pull) " +
        "VALUES (:#{#file.uuid}, :#{#file.sha}, :#{#file.filename}, :#{#file.previousFilename}, :#{#file.status}, :#{#file.additions}, :#{#file.deletions}, :#{#file.changes}, :#{#file.blobUrl}, :#{#file.rawUrl}, :#{#file.contentsUrl}, :#{#file.owner}, :#{#file.repo}, :#{#file.pull})",
        nativeQuery = true)
    void insertIgnore(PullRequestFile file);

    @Query("SELECT f " +
        "FROM PullRequestFile f " +
        "WHERE f.owner = :owner AND f.repo = :repo AND f.pull = :pull AND f.filename LIKE '%.java'")
    List<PullRequestFile> findJavaFilesByRepoAndPull(@Param("owner") String owner, @Param("repo") String repo, @Param("pull") int pull);
}
