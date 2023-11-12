package com.com2vio.repositories;

import com.com2vio.entities.CommitComment;
import com.com2vio.entities.CommitFileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommitCommentRepository extends JpaRepository<CommitComment, Integer> {

    @Query("SELECT new com.com2vio.entities.CommitFileInfo(cc.commitId, cc.file) " +
        "FROM CommitComment cc " +
        "WHERE cc.owner = ?1 AND cc.repo = ?2 AND cc.pull = ?3 " +
        "GROUP BY cc.commitId, cc.file")
    List<CommitFileInfo> findCommitFileInfo(String owner, String repo, int pull);

    List<CommitComment> findCommitCommentsByOwnerAndRepo(String owner, String repo);
}
