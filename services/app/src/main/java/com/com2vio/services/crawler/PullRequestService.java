package com.com2vio.services.crawler;

import static com.com2vio.utils.DataUtils.LOCAL_GSON;
import com.com2vio.entities.PullRequest;
import com.com2vio.entities.RepoInfo;
import com.com2vio.repositories.PullRequestRepository;
import com.com2vio.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.UUID;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
@Service
public class PullRequestService {
    private static String TOPIC_PULL = "pull";
    private static String TOPIC_STATUS = "status";
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private OkHttpClient okHttpClient;
    @Autowired
    private PullRequestRepository pullRequestRepository;
    @Value("${github.token}")
    private String token;
    @Value("${github.endpoint}")
    private String endpoint;

    @KafkaListener(topics = "repo", groupId = "pull-crawler")
    public void getPulls(String info) {
        RepoInfo repoInfo = LOCAL_GSON.fromJson(info, RepoInfo.class);
        this.fetchAllPullRequests(repoInfo.getOwner(), repoInfo.getRepo(), repoInfo.getBase(), 1);
    }

    private void fetchAllPullRequests(String owner, String repo, String base, int page) {
        try {
            String url = String.format("%s/repos/%s/%s/pulls?base=%s&state=closed&page=%d&per_page=50", endpoint, owner, repo, base, page);
            log.info(url);
            Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build();
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body().string();
            JSONArray pulls = new JSONArray(responseBody);
            if (pulls.length() > 0) {
                for (int i = 0; i < pulls.length(); i++) {
                    try {
                        JSONObject pull = pulls.getJSONObject(i);
                        PullRequest pr = extractPullRequest(pull);
                        pr.setRepo(repo);
                        pr.setOwner(owner);
                        pullRequestRepository.insertIgnore(pr);
                        String prStr = LOCAL_GSON.toJson(pr);
                        kafkaTemplate.send(TOPIC_PULL, prStr);
                    } catch (Exception e) {
                        log.warn(String.format("PR extract failed: %s/%s #%d", owner, repo, pulls.getJSONObject(i).getInt("number")));
                        e.printStackTrace();
                        // TODO Retry fetching pull info
                    }
                }
                page++;
                Thread.sleep(720); // sleep for 720ms between API requests
                if (pulls.length() >= 50) {
                    fetchAllPullRequests(owner, repo, base, page);
                } else {
                    complete(owner, repo);
                }
            } else {
                complete(owner, repo);
            }
        } catch (Exception e) {
            log.error("Fetching error", e);
        }
    }

    private void complete(String owner, String repo) {
        log.info(String.format("Fetching completed: %s/%s pull requests.", owner, repo));
        PullRequest dummyPR = new PullRequest();
        dummyPR.setNumber(-1);
        dummyPR.setRepo(repo);
        dummyPR.setOwner(owner);
        kafkaTemplate.send(TOPIC_PULL, LOCAL_GSON.toJson(dummyPR));
        RepoInfo repoInfo = new RepoInfo();
        repoInfo.setOwner(owner);
        repoInfo.setRepo(repo);
        repoInfo.setPullRequests(true);
        kafkaTemplate.send(TOPIC_STATUS, LOCAL_GSON.toJson(repoInfo));
    }

    private PullRequest extractPullRequest(JSONObject pull) throws JSONException {
        PullRequest pr = new PullRequest();
        pr.setId(pull.getLong("id"));
        pr.setUuid(UUID.randomUUID().toString());
        pr.setNodeId(pull.getString("node_id"));
        pr.setNumber(pull.getInt("number"));
        pr.setState(pull.getString("state"));
        pr.setLocked(pull.getBoolean("locked"));
        pr.setTitle(pull.getString("title"));
        pr.setBody(pull.getString("body"));
        pr.setCreatedAt(OffsetDateTime.parse(pull.getString("created_at")).toLocalDateTime());
        pr.setUpdatedAt(OffsetDateTime.parse(pull.getString("updated_at")).toLocalDateTime());
        if (!pull.isNull("closed_at")) {
            pr.setClosedAt(OffsetDateTime.parse(pull.getString("closed_at")).toLocalDateTime());
        }
        if (!pull.isNull("merged_at")) {
            pr.setMergedAt(OffsetDateTime.parse(pull.getString("merged_at")).toLocalDateTime());
        }
        pr.setMergeCommitSha(pull.optString("merge_commit_sha"));
        pr.setDraft(pull.getBoolean("draft"));
        JSONObject head = pull.getJSONObject("head");
        pr.setHead(head.toString());
        JSONObject base = pull.getJSONObject("base");
        pr.setBase(base.toString());
        pr.setUser(DataUtils.extractUser(pull.getJSONObject("user")));
        return pr;
    }
}


