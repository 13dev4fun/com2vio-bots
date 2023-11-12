package com.com2vio.services.crawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;

@Service
@Slf4j
public class CommitCommentService {

    private final JdbcTemplate jdbcTemplate;

    public CommitCommentService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insertCommitComment(String owner, String repo) {
        this.insertCommitCommentOriginal(owner, repo);
        this.insertCommitCommentFinal(owner, repo);
    }

    private void insertCommitCommentOriginal(String owner, String repo) {
        try {
            String sql = "INSERT IGNORE INTO commit_comment (commit_id, owner, repo, pull, comment_uuid, file, start_line, end_line, content) " +
                "(SELECT mr.original_commit_id, mr.owner, mr.repo, mr.pull, mr.uuid, mr.path, mr.original_start_line, mr.original_line, body " +
                "FROM " +
                "   (SELECT r.owner, " +
                "           r.repo, " +
                "           r.pull, " +
                "           r.original_commit_id, " +
                "           r.path, " +
                "           r.original_line, " +
                "           r.line, " +
                "           r.original_start_line, " +
                "           r.start_line, " +
                "           MIN(r.created_at) AS first " +
                "    FROM pull_request_comment AS r " +
                "             LEFT JOIN pull_request AS p " +
                "                       ON p.owner = r.owner AND p.repo = r.repo AND p.number = r.pull " +
                "    WHERE (r.original_commit_id IN ( " +
                "        SELECT sha " +
                "        FROM pull_request_commit c " +
                "        WHERE c.owner = r.owner " +
                "          AND c.repo = r.repo " +
                "          AND c.pull = r.pull " +
                "    )) " +
                "      AND r.original_line IS NOT NULL " +
                "      AND r.path LIKE '%.java'" +
                "      AND r.user != p.user " +
                "      AND r.side = 'RIGHT' " +
                "      AND r.owner = ? AND r.repo = ? " +
                "    GROUP BY r.owner, r.repo, r.pull, r.original_commit_id, r.path, " +
                "             r.original_line, r.line, r.original_start_line, r.start_line) tmp " +
                "       LEFT JOIN pull_request_comment AS mr " +
                "                 ON tmp.owner = mr.owner " +
                "                    AND tmp.repo = mr.repo " +
                "                    AND tmp.pull = mr.pull " +
                "                    AND tmp.original_commit_id = mr.original_commit_id " +
                "                    AND tmp.path = mr.path " +
                "                    AND (tmp.original_line = mr.original_line) " +
                "                    AND (tmp.line = mr.line OR tmp.line IS NULL) " +
                "                    AND (tmp.original_start_line = mr.original_start_line OR tmp.original_start_line IS NULL) " +
                "                    AND (tmp.start_line = mr.start_line OR tmp.start_line IS NULL) " +
                "                    AND tmp.first = mr.created_at " +
                "WHERE mr.uuid is not null);";
            int rowsInserted = jdbcTemplate.update(sql, owner, repo);
            log.info("{} rows inserted into commit_comment table", rowsInserted);
        } catch (DataAccessException e) {
            log.error("Error inserting data into commit_comment table");
            e.printStackTrace();
        }
    }

    private void insertCommitCommentFinal(String owner, String repo) {
        try {
            String sql = "INSERT IGNORE INTO commit_comment (commit_id, owner, repo, pull, comment_uuid, file, start_line, end_line, content) " +
                "(SELECT mr.commit_id, mr.owner, mr.repo, mr.pull, mr.uuid, mr.path, mr.start_line, mr.line, body " +
                "FROM " +
                "   (SELECT r.owner, " +
                "           r.repo, " +
                "           r.pull, " +
                "           r.commit_id, " +
                "           r.path, " +
                "           r.original_line, " +
                "           r.line, " +
                "           r.original_start_line, " +
                "           r.start_line, " +
                "           MIN(r.created_at) AS first " +
                "    FROM pull_request_comment AS r " +
                "             LEFT JOIN pull_request AS p " +
                "                       ON p.owner = r.owner AND p.repo = r.repo AND p.number = r.pull " +
                "    WHERE (r.commit_id IN ( " +
                "        SELECT sha " +
                "        FROM pull_request_commit c " +
                "        WHERE c.owner = r.owner " +
                "          AND c.repo = r.repo " +
                "          AND c.pull = r.pull " +
                "    )) " +
                "      AND r.original_commit_id NOT IN ( " +
                "        SELECT sha " +
                "        FROM pull_request_commit c " +
                "        WHERE c.owner = r.owner " +
                "          AND c.repo = r.repo " +
                "          AND c.pull = r.pull " +
                "    ) " +
                "      AND r.line IS NOT NULL " +
                "      AND r.path LIKE '%.java' " +
                "      AND r.user != p.user " +
                "      AND r.side = 'RIGHT' " +
                "      AND r.owner = ? " + // added owner filter
                "      AND r.repo = ? " + // added repo filter
                "    GROUP BY r.owner, r.repo, r.pull, r.commit_id, r.path, " +
                "             r.original_line, r.line, r.original_start_line, r.start_line) tmp " +
                "       LEFT JOIN pull_request_comment AS mr " +
                "                 ON tmp.owner = mr.owner " +
                "                    AND tmp.repo = mr.repo " +
                "                    AND tmp.pull = mr.pull " +
                "                    AND tmp.commit_id = mr.commit_id " +
                "                    AND tmp.path = mr.path " +
                "                    AND (tmp.original_line = mr.original_line OR tmp.original_line IS NULL) " +
                "                    AND (tmp.line = mr.line) " +
                "                    AND (tmp.original_start_line = mr.original_start_line OR tmp.original_start_line IS NULL) " +
                "                    AND (tmp.start_line = mr.start_line OR tmp.start_line IS NULL) " +
                "                    AND tmp.first = mr.created_at " +
                " WHERE mr.uuid IS NOT NULL)";
            int rowsInserted = jdbcTemplate.update(sql, owner, repo);
            log.info("{} rows inserted into commit_comment table", rowsInserted);
        } catch (DataAccessException e) {
            log.error("Error inserting data into commit_comment table");
            e.printStackTrace();
        }
    }
}
