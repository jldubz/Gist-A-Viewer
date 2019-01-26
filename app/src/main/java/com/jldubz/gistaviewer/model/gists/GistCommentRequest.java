package com.jldubz.gistaviewer.model.gists;

public class GistCommentRequest {

    private String body;

    public static GistCommentRequest fromString(String body) {
        GistCommentRequest gistCommentRequest = new GistCommentRequest();
        gistCommentRequest.setBody(body);
        return gistCommentRequest;
    }

    public GistCommentRequest() {
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
