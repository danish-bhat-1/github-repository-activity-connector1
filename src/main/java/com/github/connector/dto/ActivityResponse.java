package com.github.connector.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.connector.model.Repository;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityResponse {
    private String username;
    private List<Repository> repositories;
    private LocalDateTime fetchedAt = LocalDateTime.now();
    private int totalRepositories;
    private int totalCommits;
    private boolean rateLimited;
    private String message;
}
