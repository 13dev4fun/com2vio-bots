package com.com2vio.repositories;

import com.com2vio.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO pull_request_comment (uuid, id, node_id, diff_hunk, path, commit_id, original_commit_id, user, body, created_at, updated_at, start_line, original_start_line, start_side, line, original_line, side, owner, repo, pull, pull_request_review_id) " +
        "VALUES (:#{#comment.uuid}, :#{#comment.id}, :#{#comment.nodeId}, :#{#comment.diffHunk}, :#{#comment.path}, :#{#comment.commitId}, :#{#comment.originalCommitId}, :#{#comment.user}, :#{#comment.body}, :#{#comment.createdAt}, :#{#comment.updatedAt}, :#{#comment.startLine}, :#{#comment.originalStartLine}, :#{#comment.startSide}, :#{#comment.line}, :#{#comment.originalLine}, :#{#comment.side}, :#{#comment.owner}, :#{#comment.repo}, :#{#comment.pull}, :#{#comment.pullRequestReviewId}) ",
        nativeQuery = true)
    void insertIgnore(@Param("comment") Comment comment);
}
