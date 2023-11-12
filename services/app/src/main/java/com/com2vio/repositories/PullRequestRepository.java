package com.com2vio.repositories;

import com.com2vio.entities.PullRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import jakarta.transaction.Transactional;

@Repository
public interface PullRequestRepository extends JpaRepository<PullRequest, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO pull_request (uuid, id, number, state, locked, title, body, created_at, updated_at, closed_at, merged_at, merge_commit_sha, final_commit, draft, head, base, repo, owner, node_id, user, file_count, comment_count, commit_count) " +
        "VALUES (:#{#pullRequest.uuid}, :#{#pullRequest.id}, :#{#pullRequest.number}, :#{#pullRequest.state}, :#{#pullRequest.locked}, :#{#pullRequest.title}, :#{#pullRequest.body}, :#{#pullRequest.createdAt}, :#{#pullRequest.updatedAt}, :#{#pullRequest.closedAt}, :#{#pullRequest.mergedAt}, :#{#pullRequest.mergeCommitSha}, :#{#pullRequest.finalCommit}, :#{#pullRequest.draft}, :#{#pullRequest.head}, :#{#pullRequest.base}, :#{#pullRequest.repo}, :#{#pullRequest.owner}, :#{#pullRequest.nodeId}, :#{#pullRequest.user}, :#{#pullRequest.fileCount}, :#{#pullRequest.commentCount}, :#{#pullRequest.commitCount})",
        nativeQuery = true)
    void insertIgnore(@Param("pullRequest") PullRequest pullRequest);

    @Transactional
    @Modifying
    @Query(value = "UPDATE pull_request pr " +
        "SET final_commit = (SELECT prc.sha " +
        "                    FROM pull_request_commit prc " +
        "                    WHERE prc.pull = pr.number AND pr.owner = :owner and pr.repo = :repo" +
        "                    ORDER BY idx DESC " +
        "                    LIMIT 1) WHERE pr.final_commit is NULL", nativeQuery = true)
    void updatePullRequestFinalCommit(String owner, String repo);

    @Query("SELECT pr " +
        "FROM PullRequest pr " +
        "WHERE pr.owner = ?1 AND pr.repo = ?2 AND pr.number IN (" +
        "SELECT DISTINCT cc.pull " +
        "FROM CommitComment cc " +
        "WHERE cc.owner = ?1 AND cc.repo = ?2)")
    List<PullRequest> findPullRequestWithComments(String owner, String repo);

    @Query("SELECT e FROM PullRequest e WHERE e.owner = ?1 AND e.repo = ?2 AND e.fileCount > 0")
    Page<PullRequest> findAllByOwnerAndRepo(String owner, String repo, Pageable pageable);
}
