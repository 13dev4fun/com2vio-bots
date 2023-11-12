package com.com2vio.services.analyzer;

import static com.com2vio.utils.DataUtils.LOCAL_GSON;
import com.com2vio.entities.CommitComment;
import com.com2vio.entities.RepoInfo;
import com.com2vio.entities.ViolationCommentMatch;
import com.com2vio.entities.ViolationOnCurrent;
import com.com2vio.repositories.CommitCommentRepository;
import com.com2vio.repositories.ViolationOnCurrentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import jakarta.transaction.Transactional;

@Service
@Slf4j
public class LineMatchService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CommitCommentRepository commitCommentRepository;
    @Autowired
    private ViolationOnCurrentRepository violationRepository;

    @Value("${params.offset:3}")
    private int maxOffset;
    @Value("${repos.dir}")
    private String reposDir;

    @KafkaListener(topics = "match-event", groupId = "match-service")
    void listener(String info) {
        RepoInfo repoInfo = LOCAL_GSON.fromJson(info, RepoInfo.class);
        if (!Boolean.TRUE.equals(repoInfo.getMatched())) {
            this.matchByLines(repoInfo.getOwner(), repoInfo.getRepo());
            // update file downloading status
            repoInfo.setMatched(true);
            String msg = LOCAL_GSON.toJson(repoInfo);
            kafkaTemplate.send("status", msg);
            kafkaTemplate.send("label-event", msg);
        }
    }

    public void matchByLines(String owner, String repo) {
        List<CommitComment> commitCommentList = commitCommentRepository.findCommitCommentsByOwnerAndRepo(owner, repo);
        if (commitCommentList.isEmpty()) {
            log.info("No comments found for {}/{}", owner, repo);
            return;
        }
        commitCommentList.forEach(comment -> {
            for (int offset = 0; offset <= this.maxOffset; offset++) {
                List<ViolationOnCurrent> violations = violationRepository.getViolationCandidates(
                    owner, repo, String.valueOf(comment.getPull()), comment.getCommitId(), comment.getFile(), comment.getCommentUUID(), offset
                );
                if (!violations.isEmpty()) {
                    this.updateViolationCommentMatch(violations, comment, offset);
                } else {
                    log.info("No violations found for comment {}/{}/{} {}", owner, repo, comment.getPull(), comment.getCommentUUID(), offset);
                }
            }
        });
        log.info("Match by lines done for {}/{}", owner, repo);
    }

    @Transactional
    private void updateViolationCommentMatch(List<ViolationOnCurrent> violations, CommitComment comment, int offset) {
        List<ViolationCommentMatch> matches = new ArrayList<>();
        for (ViolationOnCurrent violation : violations) {
            if (violation.getLine() != null && !violation.getLine().isEmpty()) {
                String[] linePairs = violation.getLine().split(";");
                boolean match = false;
                int matched_vio_start = 0;
                int matched_vio_end = 0;
                for (String linePair : linePairs) {
                    if (!linePair.equals("0-0") && !linePair.isEmpty()) {
                        String[] pair = linePair.split("-");
                        int left = Integer.parseInt(pair[0]);
                        int right = Integer.parseInt(pair[1]);
                        if ((comment.getEndLine() != null && comment.getEndLine() >= (left - offset) && comment.getEndLine() <= (right + offset))
                            || (comment.getStartLine() != null && comment.getStartLine() >= (left - offset) && comment.getStartLine() <= (right + offset))) {
                            match = true;
                            matched_vio_start = left - offset;
                            matched_vio_end = right + offset;
                            break;
                        }
                    }
                }
                if (match) {
                    String filePath = String.format("%s/%s/%d/%s/%s/%s", reposDir, comment.getRepo(), comment.getPull(), comment.getCommitId(), "current", comment.getFile());
                    ViolationCommentMatch vcm = new ViolationCommentMatch();
                    vcm.setOwner(comment.getOwner());
                    vcm.setRepo(comment.getRepo());
                    vcm.setPull(String.valueOf(comment.getPull()));
                    vcm.setCommitId(comment.getCommitId());
                    vcm.setFilePath(violation.getFilePath());
                    vcm.setLine(violation.getLine());
                    vcm.setStartLine(comment.getStartLine());
                    vcm.setEndLine(comment.getEndLine());
                    vcm.setType(violation.getType());
                    vcm.setDetail(violation.getDetail());
                    vcm.setComment(comment.getContent());
                    vcm.setViolationUUID(violation.getViolationUUID());
                    vcm.setCommentUUID(comment.getCommentUUID());
                    vcm.setOffset(offset);
                    vcm.setVioCode(getLinesOfCode(filePath, matched_vio_start, matched_vio_end));
                    vcm.setCmtCode(getLinesOfCode(filePath,
                        comment.getStartLine() != null ? comment.getStartLine() : comment.getEndLine(), comment.getEndLine()));
                    matches.add(vcm);
                }
            }
        }
        this.saveViolationCommentMatches(matches);
    }
    public void saveViolationCommentMatches(List<ViolationCommentMatch> violationCommentMatches) {
        String sql = "INSERT IGNORE INTO violation_comment_match (" +
            "owner, repo, pull, commit_id, file_path, " +
            "violation_uuid, type, detail, line, comment_uuid, " +
            "start_line, end_line, comment, vio_code, cmt_code, " +
            "offset) " +
            "VALUES (?, ?, ?, ?, ?, " +
                    "?, ?, ?, ?, ?, " +
                    "?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ViolationCommentMatch vcm = violationCommentMatches.get(i);
                ps.setString(1, vcm.getOwner());
                ps.setString(2, vcm.getRepo());
                ps.setString(3, vcm.getPull());
                ps.setString(4, vcm.getCommitId());
                ps.setString(5, vcm.getFilePath());
                ps.setString(6, vcm.getViolationUUID());
                ps.setString(7, vcm.getType());
                ps.setString(8, vcm.getDetail());
                ps.setString(9, vcm.getLine());
                ps.setString(10, vcm.getCommentUUID());
                if (vcm.getStartLine() == null) {
                    ps.setNull(11, Types.INTEGER);
                } else {
                    ps.setInt(11, vcm.getStartLine());
                }
                ps.setInt(12, vcm.getEndLine());
                ps.setString(13, vcm.getComment());
                ps.setString(14, vcm.getVioCode());
                ps.setString(15, vcm.getCmtCode());
                ps.setInt(16, vcm.getOffset());
            }
            @Override
            public int getBatchSize() {
                return violationCommentMatches.size();
            }
        });
    }

    private String getLinesOfCode(String filePath, int startLine, int endLine) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder();
            String line;
            int currentLine = 1;
            while ((line = br.readLine()) != null && currentLine <= endLine) {
                if (currentLine >= startLine) {
                    sb.append(line);
                }
                currentLine++;
            }
            return sb.toString();
        } catch (IOException e) {
            log.error(e.getMessage());
            return "";
        }
    }
}
