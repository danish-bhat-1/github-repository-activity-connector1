# GitHub Repository Activity Connector

A Spring Boot application that connects to the GitHub API to fetch user repository activity, including repositories and recent commits. This tool is useful for developers, data analysts, or anyone interested in programmatically retrieving GitHub activity data.

## Features

- Fetches public and private repositories for a given GitHub user.
- Retrieves recent commits for each repository.
- Supports authentication via personal access token.
- Configurable limits for the number of repositories and commits fetched.
- REST API endpoints for integration with other tools or frontends.

## Prerequisites

- Java 8
- Maven 3.x
- A GitHub personal access token for accessing private repositories or increasing rate limits. You can generate one [here](https://github.com/settings/tokens). Ensure you grant `repo` scope for private repository access.

## Setup Instructions

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/yourusername/github-repository-activity-connector.git](https://github.com/yourusername/github-repository-activity-connector.git)
    cd github-repository-activity-connector
    ```

2.  **Build the application:**
    ```bash
    mvn clean install
    ```

3.  **Run the application:**
    ```bash
    java -jar target/github-repository-activity-connector-0.0.1-SNAPSHOT.jar
    ```
    The application will start on port 8080 by default.

## REST API Endpoints

The application exposes the following REST API endpoints:

### 1. Fetch User Activity (POST request)

This endpoint allows you to fetch a user's GitHub activity by sending a JSON request body.

-   **URL:** `POST /api/github/activity`
-   **Content-Type:** `application/json`
-   **Request Body Example:**
    ```json
    {
      "username": "octocat",
      "token": "YOUR_GITHUB_PERSONAL_ACCESS_TOKEN",
      "maxRepos": 5,
      "maxCommitsPerRepo": 20 
    }
    ```
   -   `username` (required): The GitHub username whose activity you want to fetch.
   -   `token` (required): Your GitHub personal access token. This is crucial for accessing private repositories and for higher API rate limits.
   -   `maxRepos` (optional, default: 30): The maximum number of repositories to fetch for the user.
   -   `maxCommitsPerRepo` (optional, default: 20): The maximum number of recent commits to fetch for each repository.

-   **Example using `curl`:**
    ```bash
    curl -X POST \
         -H "Content-Type: application/json" \
         -d '{
           "username": "octocat",
           "token": "YOUR_GITHUB_PERSONAL_ACCESS_TOKEN",
           "maxRepos": 5,
           "maxCommitsPerRepo": 20
         }' \
         http://localhost:8080/api/github/activity
    ```

-   **Successful Response (200 OK):**
    ```json
    {
      "username": "octocat",
      "repositories": [
        {
          "name": "Spoon-Knife",
          "url": "[https://github.com/octocat/Spoon-Knife](https://github.com/octocat/Spoon-Knife)",
          "commits": [
            {
              "sha": "a1b2c3d4...",
              "message": "Update README",
              "author": "octocat",
              "date": "2023-10-26T10:00:00"
            }
           
          ]
        }
     
      ],
      "totalRepositories": 1,
      "totalCommits": 1,
      "rateLimited": false,
      "message": "Success",
      "fetchedAt": "2023-10-26T10:30:00.123456"
    }
    ```

-   **Error Response (e.g., 429 Too Many Requests, 500 Internal Server Error):**
    ```json
    {
      "username": "octocat",
      "repositories": null,
      "totalRepositories": 0,
      "totalCommits": 0,
      "rateLimited": true,
      "message": "API rate limit exceeded for user ID 12345.",
      "fetchedAt": "2023-10-26T10:30:00.123456"
    }
    ```

### 2. Health Check

A simple endpoint to check if the application is running.

-   **URL:** `GET /api/github/health`
-   **Response:**
    ```
    GitHub Connector is running
    ```

## Postman/Insomnia Usage

You can easily test these endpoints using tools like Postman or Insomnia:

### For `POST /api/github/activity`:

1.  Set the request method to `POST`.
2.  Enter the URL: `http://localhost:8080/api/github/activity`.
3.  Go to the "Body" tab and select "raw" and "JSON" from the dropdowns.
4.  Paste the JSON request body (as shown in the example above) into the text area.
5.  Click "Send".
