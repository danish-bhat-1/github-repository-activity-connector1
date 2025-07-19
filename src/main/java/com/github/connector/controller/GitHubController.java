package com.github.connector.controller;

import com.github.connector.dto.ActivityRequest;
import com.github.connector.dto.ActivityResponse;
import com.github.connector.model.Commit;
import com.github.connector.model.Repository;
import com.github.connector.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    @Autowired
    private GitHubService gitHubService;

    @PostMapping("/activity")
    public ResponseEntity<ActivityResponse> fetchUserActivity(@Valid @RequestBody ActivityRequest request) {
        try {
            List<Repository> repositories = gitHubService.fetchUserRepositories(request.getUsername(), request.getToken(), request.getMaxRepos());
            int totalCommits = 0;

            for (Repository repo : repositories) {
                try {
                    List<Commit> commits = gitHubService.fetchRepositoryCommits(request.getUsername(), repo.getName(), request.getToken(), request.getMaxCommitsPerRepo());
                    repo.setCommits(commits);
                    totalCommits += commits != null ? commits.size() : 0;
                } catch (Exception e) {
                    System.err.println("Failed to fetch commits for " + repo.getName() + ": " + e.getMessage());
                }
            }

            ActivityResponse response = ActivityResponse.builder()
                    .username(request.getUsername())
                    .repositories(repositories)
                    .totalRepositories(repositories.size())
                    .totalCommits(totalCommits)
                    .rateLimited(false)
                    .message("Success")
                    .fetchedAt(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ActivityResponse errorResponse = ActivityResponse.builder()
                    .username(request.getUsername())
                    .repositories(null)
                    .totalRepositories(0)
                    .totalCommits(0)
                    .rateLimited(e.getMessage().contains("Rate limit"))
                    .message(e.getMessage())
                    .fetchedAt(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(e.getMessage().contains("Rate limit") ? 429 : 500)
                    .body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("GitHub Connector is running");
    }
}