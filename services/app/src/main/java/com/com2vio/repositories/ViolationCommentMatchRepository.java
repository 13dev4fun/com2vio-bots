package com.com2vio.repositories;

import com.com2vio.entities.ViolationCommentMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ViolationCommentMatchRepository extends JpaRepository<ViolationCommentMatch, String> {

    @Query("SELECT e FROM ViolationCommentMatch e WHERE e.owner = ?1 AND e.repo = ?2 AND e.type <> 'Cognitive Complexity of methods should not be too high'")
    Page<ViolationCommentMatch> findAllByOwnerAndRepoOrderByLabelDesc(String owner, String repo, Pageable pageable);
}
