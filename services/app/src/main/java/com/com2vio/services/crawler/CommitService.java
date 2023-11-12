package com.com2vio.services.crawler;

import static com.com2vio.utils.DataUtils.LOCAL_GSON;
import com.com2vio.entities.Commit;
import com.com2vio.entities.PullRequest;
import com.com2vio.entities.RepoInfo;
import com.com2vio.repositories.CommitRepository;
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
import java.util.UUID;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
@Slf4j
public class CommitService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private OkHttpClient okHttpClient;
    @Autowired
    private CommitRepository commitRepository;
    @Autowired
    private PullRequestRepository pullRequestRepository;
    @Value("${github.token}")
    private String token;
    @Value("${github.endpoint}")
    private String endpoint;

    @KafkaListener(topics = "pull", groupId = "commit-crawler")
    void listener(String pull) {
        PullRequest pr = LOCAL_GSON.fromJson(pull, PullRequest.class);
        if (pr.getNumber() == -1) {
            // TODO update repo commit counts
            pullRequestRepository.updatePullRequestFinalCommit(pr.getOwner(), pr.getRepo());
            RepoInfo repoInfo = new RepoInfo();
            repoInfo.setOwner(pr.getOwner());
            repoInfo.setRepo((pr.getRepo()));
            repoInfo.setCommits(true);
            kafkaTemplate.send("status", LOCAL_GSON.toJson(repoInfo));
        } else {
            fetchAllCommits(pr, 1);
        }
    }

    private void fetchAllCommits(PullRequest pr, int page) {
        try {
            String url = String.format("%s/repos/%s/%s/pulls/%d/commits?page=%d&per_page=100", endpoint, pr.getOwner(), pr.getRepo(), pr.getNumber(), page);
            Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build();
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body().string();
            JSONArray commitJsons = new JSONArray(responseBody);
            if (commitJsons.length() > 0) {
                for (int i = 0; i < commitJsons.length(); i++) {
                    try {
                        JSONObject commitJson = commitJsons.getJSONObject(i);
                        Commit commit = extractCommit(commitJson);
                        commit.setRepo(pr.getRepo());
                        commit.setOwner(pr.getOwner());
                        commit.setUuid(String.valueOf(UUID.randomUUID()));
                        commit.setPull(pr.getNumber());
                        commit.setIdx(i);
                        commitRepository.insertIgnore(commit);
                    } catch (Exception e) {
                        log.warn(String.format("PR extract failed: %s/%s #%d", pr.getOwner(), pr.getRepo(), pr.getNumber()));
                        e.printStackTrace();
                    }
                }
                // TODO parameter type error for inserting all
                page++;
                Thread.sleep(720); // sleep for 720ms between API requests
                if (commitJsons.length() >= 100) {
                    fetchAllCommits(pr, page);
                } else {
                    log.info(String.format("Commits Done: %s/%s/%d.", pr.getOwner(), pr.getRepo(), pr.getNumber()));
                }
            } else {
                log.info(String.format("Commits Done: %s/%s/%d.", pr.getOwner(), pr.getRepo(), pr.getNumber()));
            }
        } catch (Exception e) {
            log.error(String.format("Commits error: %s/%s/%d.", pr.getOwner(), pr.getRepo(), pr.getNumber()));
            log.error(e.toString());
        }
    }

    private Commit extractCommit(JSONObject commitJson) throws JSONException {
        Commit commit = new Commit();
        commit.setSha(commitJson.getString("sha"));
        commit.setNodeId(commitJson.getString("node_id"));
        commit.setCommit(LOCAL_GSON.toJson(commitJson.getJSONObject("commit")));
        commit.setUrl(commitJson.getString("url"));
        commit.setAuthor(commitJson.has("author") && !commitJson.isNull("author")? DataUtils.extractUser(commitJson.getJSONObject("author")) : null);
        commit.setCommitter(commitJson.has("committer") && !commitJson.isNull("committer") ? DataUtils.extractUser(commitJson.getJSONObject("committer")) : null);
        commit.setParents(commitJson.getString("parents"));
        return commit;
    }
}
