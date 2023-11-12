package com.com2vio.repositories;

import com.com2vio.entities.RepoInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
public interface RepoInfoRepository extends JpaRepository<RepoInfo, String> {

    @Transactional
    @Modifying
    @Query(value = "INSERT IGNORE INTO repo_info (uuid, owner, repo, base) " +
        "VALUES (:#{#repoInfo.uuid}, :#{#repoInfo.owner}, :#{#repoInfo.repo}, :#{#repoInfo.base})",
        nativeQuery = true)
    void insertIgnore(RepoInfo repoInfo);

    @Transactional
    @Modifying
    @Query("UPDATE RepoInfo r SET " +
        "r.repo = COALESCE(:#{#repo.repo}, r.repo), " +
        "r.owner = COALESCE(:#{#repo.owner}, r.owner), " +
        "r.base = COALESCE(:#{#repo.base}, r.base), " +
        "r.pullRequests = COALESCE(:#{#repo.pullRequests}, r.pullRequests), " +
        "r.comments = COALESCE(:#{#repo.comments}, r.comments), " +
        "r.commits = COALESCE(:#{#repo.commits}, r.commits), " +
        "r.files = COALESCE(:#{#repo.files}, r.files), " +
        "r.filesDownloaded = COALESCE(:#{#repo.filesDownloaded}, r.filesDownloaded), " +
        "r.commitComments = COALESCE(:#{#repo.commitComments}, r.commitComments), " +
        "r.matched = COALESCE(:#{#repo.matched}, r.matched), " +
        "r.labelled = COALESCE(:#{#repo.labelled}, r.labelled), " +
        "r.postProcessed = COALESCE(:#{#repo.postProcessed}, r.postProcessed) " +
        "WHERE r.owner = :#{#repo.owner} and r.repo = :#{#repo.repo}")
    void updateRepoInfo(RepoInfo repo);

    RepoInfo findByOwnerAndRepo(String owner, String repo);
}
