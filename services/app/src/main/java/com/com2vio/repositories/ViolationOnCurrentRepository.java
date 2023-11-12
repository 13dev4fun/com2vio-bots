package com.com2vio.repositories;

import com.com2vio.entities.Violation;
import com.com2vio.entities.ViolationOnCurrent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ViolationOnCurrentRepository extends JpaRepository<ViolationOnCurrent, Long> {

    @Query(value = "SELECT v.* " +
        "FROM violation_on_current v " +
        "WHERE v.owner = :owner AND v.repo = :repo " +
        "AND v.pr = :pull " +
        "AND v.commit_id = :commitId " +
        "AND v.file_path = :filePath " +
        "AND v.violation_uuid NOT IN (" +
        "    SELECT vcm.violation_uuid " +
        "    FROM violation_comment_match vcm " +
        "    WHERE vcm.comment_uuid = :commentUuid " +
        "    AND vcm.offset < :offset)", nativeQuery = true)
    List<ViolationOnCurrent> getViolationCandidates(@Param("owner") String owner,
                                           @Param("repo") String repo,
                                           @Param("pull") String pull,
                                           @Param("commitId") String commitId,
                                           @Param("filePath") String filePath,
                                           @Param("commentUuid") String commentUuid,
                                           @Param("offset") int offset);
}
