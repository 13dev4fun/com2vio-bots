package com.com2vio.services.crawler;

import static com.com2vio.utils.DataUtils.LOCAL_GSON;
import com.com2vio.entities.RepoInfo;
import com.com2vio.repositories.RepoInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import jakarta.transaction.Transactional;

@Service
@Slf4j
public class RepoService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private RepoInfoRepository repoInfoRepository;

    @Transactional
    public RepoInfo saveRepo(RepoInfo repoInfo) {
        try {
            RepoInfo result = repoInfoRepository.findByOwnerAndRepo(repoInfo.getOwner(), repoInfo.getRepo());
            if (result == null) {
                repoInfo.setUuid(String.valueOf(UUID.randomUUID()));
                repoInfoRepository.insertIgnore(repoInfo);
                kafkaTemplate.send("repo", LOCAL_GSON.toJson(repoInfo));
            }
            return repoInfoRepository.findByOwnerAndRepo(repoInfo.getOwner(), repoInfo.getRepo());
        } catch (Exception e) {
            return null;
        }
    }

    public List<RepoInfo> getRepos() {
        return repoInfoRepository.findAll();
    }

    public RepoInfo getRepoByOwnerAndRepo(String owner, String repo) {
        return repoInfoRepository.findByOwnerAndRepo(owner, repo);
    }
}
