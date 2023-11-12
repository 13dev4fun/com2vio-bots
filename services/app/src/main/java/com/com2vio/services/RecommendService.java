package com.com2vio.services;

import com.com2vio.models.Rule;
import com.com2vio.models.RuleResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class RecommendService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<RuleResult> getRuleResults(String owner, String repo, int size, int page) {
        String sql = "SELECT d.*, issueTrackerTest.issue_type.category, issueTrackerTest.issue_type.severity " +
            "from (" +
            "         select tff.type," +
            "                trt.tc," +
            "                ROUND(trt.tc / trt.occurrences, 2) as tr," +
            "                trt.te," +
            "                trt.occurrences," +
            "                ROUND(rf, 2) as rf," +
            "                total," +
            "                ROUND(rf * (trt.tc / trt.occurrences), 3) as score" +
            "         from (select tf, a.type, total, tf / total as rf" +
            "               from (select count(*) as tf, type" +
            "                     from (" +
            "                              select vpr.category," +
            "                                     vpr.type," +
            "                                     vpr.detail," +
            "                                     vpr.violation_uuid," +
            "                                     vpr.repo," +
            "                                     vpr.pr," +
            "                                     vpr.file_path," +
            "                                     vpr.matched_violation_uuid," +
            "                                     vpr.commit_id" +
            "                              from issueTrackerTest.violation_pr vpr" +
            "                              where repo = :repo" +
            "                                and code_source = 'current'" +
            "                                and vpr.match_status = 'solved'" +
            "                                and vpr.type <> 'Cognitive Complexity of methods should not be too high') tmp" +
            "                     GROUP BY type) a" +
            "                        left join (select count(distinct (violation_uuid)) as total, type" +
            "                                   from issueTrackerTest.violation_pr" +
            "                                   where repo = :repo" +
            "                                     and code_source = 'current'" +
            "                                     and type <> 'Cognitive Complexity of methods should not be too high'" +
            "                                   group by type) b on a.type = b.type) tff" +
            "                  left join (select * from codeReviewAnalyzer.violation_tr_per_project where repo = :repo) trt " +
            "on tff.type = trt.type) d " +
            "LEFT JOIN issueTrackerTest.issue_type ON d.type = issueTrackerTest.issue_type.type " +
            "ORDER BY score DESC, rf DESC, te ASC " +
            "LIMIT :size " +
            "OFFSET :page";
        MapSqlParameterSource paramMap = new MapSqlParameterSource();
        paramMap.addValue("owner", owner);
        paramMap.addValue("repo", repo);
        paramMap.addValue("size", size);
        paramMap.addValue("page", Math.max(0, page - 1) * size);
        List<RuleResult> rules = jdbcTemplate.query(sql, paramMap, (rs, rowNum) -> {
            RuleResult rule = new RuleResult();
            rule.setType(rs.getString("type"));
            rule.setCategory(rs.getString("category"));
            rule.setSeverity(rs.getString("severity"));
            rule.setTa(rs.getInt("te"));
            rule.setTc(rs.getInt("tc"));
            rule.setTf(rs.getInt("rf"));
            rule.setTr(rs.getDouble("tr"));
            rule.setOrder(rowNum);
            return rule;
        });
        return rules;
    }

    public List<Rule> getRecommendedRules(String owner, String repo, int size) {
        String sql = "SELECT it.type, it.category, ta, tc, tc / ta as tr " +
            "FROM issue_type it LEFT JOIN (" +
            "    SELECT type, repo, COUNT(*) AS ta " +
            "    FROM rules_for_caring_or_not " +
            "    WHERE owner = :owner and repo = :repo " +
            "    GROUP BY type, repo " +
            ") tep ON tep.type = it.type " +
            "LEFT JOIN (" +
            "    SELECT type, repo, COUNT(*) AS tc " +
            "    FROM rules_for_caring_or_not " +
            "    WHERE concern = 1 and owner = :owner and repo = :repo " +
            "    GROUP BY type, repo " +
            ") tcp ON tcp.type = it.type " +
            "ORDER BY tr DESC, CASE WHEN ta IS NULL THEN 1 ELSE 0 END, ta IS NULL, ta ASC " +
            "LIMIT :size";

        MapSqlParameterSource paramMap = new MapSqlParameterSource();
        paramMap.addValue("owner", owner);
        paramMap.addValue("repo", repo);
        paramMap.addValue("size", size);

        List<Rule> rules = jdbcTemplate.query(sql, paramMap, (rs, rowNum) -> {
            Rule rule = new Rule();
            rule.setType(rs.getString("type"));
            rule.setCategory(rs.getString("category"));
            rule.setTa(rs.getInt("ta"));
            rule.setTc(rs.getInt("tc"));
            rule.setTr(rs.getDouble("tr"));
            rule.setOrder(rowNum);
            return rule;
        });
        return rules;
    }
}
