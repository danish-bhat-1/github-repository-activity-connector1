package com.github.connector.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.connector.model.Commit;
import com.github.connector.model.Repository;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitHubService {
    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String USERS_PATH = "/users/";
    private static final String REPOS_PATH = "/repos/";
    private static final String REPOS_SUFFIX = "/repos?type=all&sort=updated&per_page=100";
    private static final String COMMITS_SUFFIX = "/commits?per_page=100";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "token ";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "application/vnd.github.v3+json";
    private static final String LINK_HEADER = "Link";
    private static final String REL_NEXT_REGEX = "<([^>]+)>; rel=\"next\"";
    private static final String RATE_LIMIT_EXCEEDED_MSG = "Rate limit exceeded";
    private static final String USER_NOT_FOUND_MSG = "User not found: ";
    private static final String FAILED_FETCH_REPOS_MSG = "Failed to fetch repositories: ";
    private static final int RATE_LIMIT_STATUS = 403;
    private static final int NOT_FOUND_STATUS = 404;
    private static final int OK_STATUS = 200;

    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;

    public GitHubService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClients.createDefault();
    }

    @PreDestroy
    public void cleanup() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    public List<Repository> fetchUserRepositories(String username, String token, int maxRepos) throws IOException {
        String url = GITHUB_API_BASE + USERS_PATH + username + REPOS_SUFFIX;
        return fetchPaginatedData(url, token, maxRepos, new TypeReference<List<Repository>>() {}, username);
    }

    public List<Commit> fetchRepositoryCommits(String username, String repoName, String token, int maxCommits) throws IOException {
        String url = GITHUB_API_BASE + REPOS_PATH + username + "/" + repoName + COMMITS_SUFFIX;
        return fetchPaginatedData(url, token, maxCommits, new TypeReference<List<Commit>>() {}, null);
    }

    private <T> List<T> fetchPaginatedData(String initialUrl, String token, int maxItems,
                                           TypeReference<List<T>> typeRef, String username) throws IOException {
        List<T> allItems = new ArrayList<>();
        String url = initialUrl;

        while (url != null && allItems.size() < maxItems) {
            HttpGet request = new HttpGet(url);
            if (token != null && !token.isEmpty()) {
                request.setHeader(AUTHORIZATION_HEADER, TOKEN_PREFIX + token);
            }
            request.setHeader(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == RATE_LIMIT_STATUS) {
                    throw new RuntimeException(RATE_LIMIT_EXCEEDED_MSG);
                }
                if (statusCode == NOT_FOUND_STATUS && username != null) {
                    throw new RuntimeException(USER_NOT_FOUND_MSG + username);
                }
                if (statusCode != OK_STATUS) {
                    if (username != null) {
                        throw new RuntimeException(FAILED_FETCH_REPOS_MSG + statusCode);
                    } else {
                        return allItems;
                    }
                }

                List<T> items = objectMapper.readValue(responseBody, typeRef);
                for (T item : items) {
                    if (allItems.size() >= maxItems)
                        break;
                    allItems.add(item);
                }

                url = getNextPageUrl(response);
            }
        }

        return allItems;
    }

    private String getNextPageUrl(HttpResponse response) {
        String linkHeader = response.getFirstHeader(LINK_HEADER) != null ?
                response.getFirstHeader(LINK_HEADER).getValue() : null;

        if (linkHeader != null) {
            Pattern pattern = Pattern.compile(REL_NEXT_REGEX);
            Matcher matcher = pattern.matcher(linkHeader);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }
}