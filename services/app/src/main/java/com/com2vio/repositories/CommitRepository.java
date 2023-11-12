package com.com2vio.repositories;

import com.com2vio.entities.Commit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
public interface CommitRepository extends JpaRepository<Commit, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO pull_request_commit (uuid, sha, node_id, url, commit, author, committer, parents, owner, repo, pull, idx) " +
        "VALUES (:#{#obj.uuid}, :#{#obj.sha}, :#{#obj.nodeId}, :#{#obj.url}, :#{#obj.commit}, :#{#obj.author}, :#{#obj.committer}, :#{#obj.parents}, :#{#obj.owner}, :#{#obj.repo}, :#{#obj.pull}, :#{#obj.idx})",
        nativeQuery = true)
    void insertIgnore(@Param("obj") Commit commit);
}
