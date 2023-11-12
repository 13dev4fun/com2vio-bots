package com.com2vio.services.analyzer;

import static com.com2vio.utils.DataUtils.LOCAL_GSON;
import com.com2vio.entities.RepoInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import jakarta.transaction.Transactional;

@Service
@Slf4j
public class PostprocessingService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @KafkaListener(topics = "postprocessing-event", groupId = "postprocessing-service")
    void listener(String info) {
        try {
            RepoInfo repoInfo = LOCAL_GSON.fromJson(info, RepoInfo.class);
            this.doPostProcessing(repoInfo.getOwner(), repoInfo.getRepo());
            repoInfo.setPostProcessed(true);
            kafkaTemplate.send("status", LOCAL_GSON.toJson(repoInfo));
        } catch (Exception e) {
            log.error("Failed to handle postprocessing-event. Caused by {}", e.getMessage());
        }
    }

    @Transactional
    private void doPostProcessing(String owner, String repo) {
        this.insertCRCRelatedSurvivalViolations(owner, repo);
        this.insertCRCRelatedNewViolations(owner, repo);
        this.insertCRCNotRelatedNewViolations(owner, repo);
    }

    public void insertCRCRelatedSurvivalViolations(String owner, String repo) {
        // insert CRC-related + Survival violations
        String sql = "INSERT IGNORE INTO rules_for_caring_or_not (code_source, matched_violation_uuid, " +
            "violation_uuid, category, match_status, type, owner, repo, person_type, concern) " +
            "SELECT code_source, matched_violation_uuid, violation_uuid, category, match_status, type, owner, repo, 'reviewer', 1 " +
            "FROM violation " +
            "WHERE code_source = 'current' AND owner = :owner AND repo = :repo " +
            "AND matched_violation_uuid IN " +
            "(SELECT violation_uuid FROM violation WHERE code_source = 'base') " +
            "AND violation_uuid IN (SELECT violation_uuid FROM violation_comment_match WHERE label = 1 " +
            "AND owner = :owner AND repo = :repo)";

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("repo", repo);
        paramMap.put("owner", owner);
        int rowsAffected = jdbcTemplate.update(sql, new MapSqlParameterSource(paramMap));
        log.info("{} CRC-related & Survival Violations found on {}/{}", rowsAffected, owner, repo);
    }

    public void insertCRCRelatedNewViolations(String owner, String repo) {
        // insert CRC-related + New violations
        String sql = "INSERT IGNORE INTO rules_for_caring_or_not (code_source, matched_violation_uuid, " +
            "violation_uuid, category, match_status, type, owner, repo, person_type, concern) " +
            "SELECT code_source, matched_violation_uuid, violation_uuid, category, match_status, type, owner, repo, 'reviewer', 1 " +
            "FROM violation " +
            "WHERE code_source = 'current' AND owner = :owner AND repo = :repo " +
            "AND match_status = 'add' " +
            "AND violation_uuid IN (SELECT violation_uuid FROM violation_comment_match WHERE label = 1 " +
            "AND owner = :owner AND repo = :repo)";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("repo", repo);
        paramMap.put("owner", owner);
        int rowsAffected = jdbcTemplate.update(sql, new MapSqlParameterSource(paramMap));
        log.info("{} CRC-related & New Violations found on {}/{}", rowsAffected, owner, repo);
    }

    public void insertCRCNotRelatedNewViolations(String owner, String repo) {
        String sql = "INSERT IGNORE INTO rules_for_caring_or_not (code_source, matched_violation_uuid, " +
            "violation_uuid, category, match_status, type, owner, repo, person_type, concern) " +
            "SELECT code_source, matched_violation_uuid, violation_uuid, category, match_status, type, owner, repo, 'reviewer', 0 " +
            "FROM violation " +
            "WHERE code_source = 'current' AND owner = :owner AND repo = :repo " +
            "AND match_status = 'add' " +
            "AND violation_uuid NOT IN (SELECT violation_uuid FROM violation_comment_match WHERE label = 1 " +
            "AND owner = :owner AND repo = :repo)";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("repo", repo);
        paramMap.put("owner", owner);
        int rowsAffected = jdbcTemplate.update(sql, new MapSqlParameterSource(paramMap));
        log.info("{} NOT CRC-related & New Violations found on {}/{}", rowsAffected, owner, repo);
    }
}
