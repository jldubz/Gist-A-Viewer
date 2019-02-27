package com.jldubz.gistaviewer.model.gists;

import com.google.gson.annotations.SerializedName;
import com.jldubz.gistaviewer.model.GitHubUser;

import java.util.Date;

import androidx.annotation.Keep;

@Keep
public class GistComment {

    /*
    Sample data from: https://developer.github.com/v3/gists/comments/#list-comments-on-a-gist
    {
        "id": 1,
        "node_id": "MDExOkdpc3RDb21tZW50MQ==",
        "url": "https://api.github.com/gists/a6db0bec360bb87e9418/comments/1",
        "body": "Just commenting for the sake of commenting",
        "user": {
          "login": "octocat",
          "id": 1,
          "node_id": "MDQ6VXNlcjE=",
          "avatar_url": "https://github.com/images/error/octocat_happy.gif",
          "gravatar_id": "",
          "url": "https://api.github.com/users/octocat",
          "html_url": "https://github.com/octocat",
          "followers_url": "https://api.github.com/users/octocat/followers",
          "following_url": "https://api.github.com/users/octocat/following{/other_user}",
          "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
          "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
          "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
          "organizations_url": "https://api.github.com/users/octocat/orgs",
          "repos_url": "https://api.github.com/users/octocat/repos",
          "events_url": "https://api.github.com/users/octocat/events{/privacy}",
          "received_events_url": "https://api.github.com/users/octocat/received_events",
          "type": "User",
          "site_admin": false
        },
        "created_at": "2011-04-18T23:23:56Z",
        "updated_at": "2011-04-18T23:23:56Z"
      }
    */

    private int id;
    private String body;
    private GitHubUser user;
    @SerializedName("created_at")
    private Date createdAt;
    // updated

    public GistComment() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public GitHubUser getUser() {
        return user;
    }

    public void setUser(GitHubUser user) {
        this.user = user;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
