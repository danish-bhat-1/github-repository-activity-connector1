package com.github.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubApiResponse<T> {
    private List<T> data;
    private String nextPageUrl;
    private boolean hasMore;
    private int totalCount;
}
