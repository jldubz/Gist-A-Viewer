package com.jldubz.gistaviewer.model;

public class Constants {

    public static final String URL_GITHUB = "https://api.github.com";
    public static final String USERNAME_ERROR = "Please enter a valid username";
    public static final String TOKEN_ERROR = "Please enter a valid access token";
    public static final String NEED_LOGIN_ERROR = "Please login first";
    public static final String INVALID_GIST_ID_ERROR = "Invalid Gist ID\nPlease close this page and try to open the Gist again.";
    public static final String RATELIMIT_ERROR = "Uh Oh! \nIt looks like you exceeded your API rate limit.\nTry again after ";

    public static final String HEADER_RATELIMIT_LIMIT = "X-RateLimit-Limit";
    public static final String HEADER_RATELIMIT_RESET = "X-RateLimit-Reset";
}
