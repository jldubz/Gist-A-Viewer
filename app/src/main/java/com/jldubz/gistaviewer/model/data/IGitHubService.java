package com.jldubz.gistaviewer.model.data;

import com.jldubz.gistaviewer.model.gists.Gist;
import com.jldubz.gistaviewer.model.gists.GistComment;
import com.jldubz.gistaviewer.model.GitHubUser;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IGitHubService {

    @GET("/gists/public?per_page=50")
    Call<List<Gist>> getPublicGists(@Query("page") int pageNum);

    @GET("/gists?per_page=50")
    Call<List<Gist>> getYourGists(@Query("page") int pageNum);

    @GET("/gists/starred?per_page=50")
    Call<List<Gist>> getStarredGists(@Query("page") int pageNum);

    @GET("/gists/{gistId}")
    Call<Gist> getGistById(@Path("gistId") String gistId);

    @GET("/gists/{gistId}/comments")
    Call<List<GistComment>> getGistCommentsById(@Path("gistId") String gistId);

    @GET("/gists/{gistId}/star")
    Call<ResponseBody> getGistStarById(@Path("gistId") String gistId);

    @GET("/user")
    Call<GitHubUser> getLoggedInUser();

    @PUT("/gists/{gistId}/star")
    Call<ResponseBody> starGistById(@Path("gistId") String gistId);

    @DELETE("/gists/{gistId}/star")
    Call<ResponseBody> unstarGistById(@Path("gistId") String gistId);

    @POST("/gists/{gistId}/comments")
    Call<GistComment> createCommentOnGist(@Path("gistId") String gistId, @Body GistComment comment);

}
