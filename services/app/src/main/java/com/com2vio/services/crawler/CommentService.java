package com.com2vio.services.crawler;

import static com.com2vio.utils.DataUtils.LOCAL_GSON;
import com.com2vio.entities.Comment;
import com.com2vio.entities.PullRequest;
import com.com2vio.entities.RepoInfo;
import com.com2vio.repositories.CommentRepository;
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
public class CommentService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private OkHttpClient okHttpClient;
    @Autowired
    private CommentRepository commentRepository;
    @Value("${github.token}")
    private String token;
    @Value("${github.endpoint}")
    private String endpoint;

    @KafkaListener(topics = "pull", groupId = "comment-crawler")
    void listener(String pull) {
        PullRequest pr = LOCAL_GSON.fromJson(pull, PullRequest.class);
        if (pr.getNumber() == -1) {
            RepoInfo repoInfo = new RepoInfo();
            repoInfo.setOwner(pr.getOwner());
            repoInfo.setRepo((pr.getRepo()));
            repoInfo.setComments(true);
            kafkaTemplate.send("status", LOCAL_GSON.toJson(repoInfo));
        } else {
            fetchAllComments(pr, 1);
        }
    }

    private void fetchAllComments(PullRequest pr, int page) {
        try {
            String url = String.format("%s/repos/%s/%s/pulls/%d/comments?page=%d&per_page=100", endpoint, pr.getOwner(), pr.getRepo(), pr.getNumber(), page);
            Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build();
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body().string();
            JSONArray commentJsons = new JSONArray(responseBody);
            if (commentJsons.length() > 0) {
                for (int i = 0; i < commentJsons.length(); i++) {
                    try {
                        JSONObject commentJson = commentJsons.getJSONObject(i);
                        Comment comment = extractComment(commentJson);
                        comment.setRepo(pr.getRepo());
                        comment.setOwner(pr.getOwner());
                        comment.setUuid(String.valueOf(UUID.randomUUID()));
                        comment.setPull(pr.getNumber());
                        commentRepository.insertIgnore(comment);
                    } catch (Exception e) {
                        log.warn("PR extract failed: {}/{}/{}", pr.getOwner(), pr.getRepo(), pr.getNumber());
                        e.printStackTrace();
                    }
                }
                // TODO parameter type error for inserting all
                page++;
                Thread.sleep(720); // sleep for 720ms between API requests
                if (commentJsons.length() >= 100) {
                    fetchAllComments(pr, page);
                } else {
                    log.info("Comments Done: {}/{}/{}.", pr.getOwner(), pr.getRepo(), pr.getNumber());
                }
            } else {
                log.info("Comments Done: {}/{}/{}.", pr.getOwner(), pr.getRepo(), pr.getNumber());
            }
        } catch (Exception e) {
            log.error("Comments error: {}/{}/{}.", pr.getOwner(), pr.getRepo(), pr.getNumber());
            log.error(e.toString());
        }
    }

    private Comment extractComment(JSONObject commentJson) throws JSONException {
        Comment comment = new Comment();
        comment.setId(commentJson.getInt("id"));
        comment.setNodeId(commentJson.getString("node_id"));
        comment.setDiffHunk(commentJson.getString("diff_hunk"));
        comment.setPath(commentJson.getString("path"));
        comment.setCommitId(commentJson.getString("commit_id"));
        comment.setOriginalCommitId(commentJson.getString("original_commit_id"));
        comment.setUser(commentJson.has("user") && !commentJson.isNull("user")? DataUtils.extractUser(commentJson.getJSONObject("user")) : null);
        comment.setBody(commentJson.getString("body"));
        comment.setCreatedAt(OffsetDateTime.parse(commentJson.getString("created_at")).toLocalDateTime());
        comment.setUpdatedAt(OffsetDateTime.parse(commentJson.getString("updated_at")).toLocalDateTime());
        comment.setStartLine(commentJson.optInt("start_line", -1) != -1 ? commentJson.getInt("start_line") : null);
        comment.setOriginalStartLine(commentJson.optInt("original_start_line", -1) != -1 ? commentJson.getInt("original_start_line") : null);
        String startSide = commentJson.getString("start_side");
        if(startSide.equals("null")){
            startSide = null;
        }
        comment.setStartSide(startSide);
        comment.setLine(commentJson.optInt("line", -1) != -1 ? commentJson.getInt("line") : null);
        comment.setOriginalLine(commentJson.optInt("original_line", -1) != -1 ? commentJson.getInt("original_line") : null);
        String side = commentJson.getString("side");
        if(side.equals("null")){
            side = null;
        }
        comment.setSide(side);
        comment.setPullRequestReviewId(commentJson.optInt("pull_request_review_id", -1) != -1 ? commentJson.getInt("pull_request_review_id") : null);
        return comment;
    }
}
