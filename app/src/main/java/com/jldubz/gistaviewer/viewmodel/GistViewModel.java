package com.jldubz.gistaviewer.viewmodel;

import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jldubz.gistaviewer.R;
import com.jldubz.gistaviewer.model.Constants;
import com.jldubz.gistaviewer.model.data.BasicAuthInterceptor;
import com.jldubz.gistaviewer.model.data.ZeroContentLengthInterceptor;
import com.jldubz.gistaviewer.model.gists.Gist;
import com.jldubz.gistaviewer.model.gists.GistComment;
import com.jldubz.gistaviewer.model.data.IGitHubService;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GistViewModel extends ViewModel {

    private MutableLiveData<Gist> mGist;
    private MutableLiveData<List<GistComment>> mComments = new MutableLiveData<>();
    private MutableLiveData<Integer> mProgressBarVisibility = new MutableLiveData<>();
    private MutableLiveData<Integer> mCommentsProgressBarVisibility = new MutableLiveData<>();
    private MutableLiveData<String> mErrorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsGistStarred = new MutableLiveData<>();

    private String mUsername;
    private String mToken;
    private String mGistId;
    private int mGistCommentPrevPage;

    private IGitHubService mGitHubService;

    public GistViewModel() {
        super();
        initAnonService();
    }

    private void initAnonService() {
        mProgressBarVisibility.setValue(View.GONE);
        mCommentsProgressBarVisibility.setValue(View.GONE);
        mErrorMessage.setValue(null);
        mIsGistStarred.setValue(false);
        mGist = null;
        mComments = new MutableLiveData<>();
        mUsername = null;
        mToken = null;

        Gson gson = new GsonBuilder()
                .setDateFormat(R.string.date_format)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_GITHUB)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        mGitHubService = retrofit.create(IGitHubService.class);
    }

    public void setGistId(String mGistId) {
        this.mGistId = mGistId;
    }

    public void setCredentials(String username, String token) {

        if (username.isEmpty() || token.isEmpty()) {
            return;
        }

        mUsername = username;
        mToken = token;

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(mUsername, mToken))
                .build();

        Gson gson = new GsonBuilder()
                .setDateFormat(R.string.date_format)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_GITHUB)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        mGitHubService = retrofit.create(IGitHubService.class);
    }

    public LiveData<Gist> getGist() {
        if (mGist == null) {
            mGist = new MutableLiveData<>();
            loadGist();
        }

        return mGist;
    }

    public LiveData<List<GistComment>> getComments() { return mComments; }

    public LiveData<Integer> getProgressBarVisibility() {
        return mProgressBarVisibility;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }

    public LiveData<Boolean> getStarredState() {
        return mIsGistStarred;
    }

    private void loadGist() {

        if (mGistId.isEmpty()) {
            showError(Constants.INVALID_GIST_ID_ERROR);
            return;
        }

        mProgressBarVisibility.postValue(View.VISIBLE);
        mGitHubService.getGistById(mGistId).enqueue(new Callback<Gist>() {
            @Override
            public void onResponse(@NonNull Call<Gist> call, @NonNull Response<Gist> response) {
                mProgressBarVisibility.postValue(View.GONE);
                if (!response.isSuccessful()) {
                    onResponseError(response);
                    return;
                }

                mGist.postValue(response.body());

                loadCommentPageCount();
                getGistStar();
            }

            @Override
            public void onFailure(@NonNull Call<Gist> call, @NonNull Throwable t) {
                showError(t.getLocalizedMessage());
            }
        });

    }

    private void loadCommentPageCount() {
        if (mGistId.isEmpty()) {
            return;
        }

        mCommentsProgressBarVisibility.postValue(View.VISIBLE);
        mGitHubService.getGistCommentsHeaderById(mGistId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    onResponseError(response);
                    return;
                }

                //check to see if there is a "next" page
                String linkHeader = response.headers().get("Link");
                if (linkHeader != null) {
                    int lastLinkIndex = linkHeader.indexOf("; rel=\"last\"");
                    if (lastLinkIndex >= 0) {
                        int lastPageNumberIndex = linkHeader.lastIndexOf("page=");
                        String lastPageNum = linkHeader.substring(lastPageNumberIndex+5, linkHeader.indexOf(">", lastPageNumberIndex));
                        mGistCommentPrevPage = Integer.parseInt(lastPageNum);
                    }
                    else {
                        mGistCommentPrevPage = 0;
                    }

                }
                else {
                    mGistCommentPrevPage = 0;
                }

                loadMoreComments();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showError(t.getLocalizedMessage());
            }
        });
    }

    public void loadMoreComments() {

        if (mGistId.isEmpty()) {
            return;
        }

        if (mGistCommentPrevPage == 0){
            return;
        }

        mCommentsProgressBarVisibility.postValue(View.VISIBLE);
        mGitHubService.getGistCommentsById(mGistId, mGistCommentPrevPage).enqueue(new Callback<List<GistComment>>() {
            @Override
            public void onResponse(@NonNull Call<List<GistComment>> call, @NonNull Response<List<GistComment>> response) {
                mCommentsProgressBarVisibility.postValue(View.GONE);
                if (!response.isSuccessful()) {
                    onResponseError(response);
                    return;
                }

                mGistCommentPrevPage--;

                List<GistComment> currentList = mComments.getValue();
                if (currentList == null) {
                    currentList = new ArrayList<>();
                }

                if (response.body() != null) {
                    List<GistComment> comments = new ArrayList<>(response.body());
                    Collections.reverse(comments);
                    currentList.addAll(comments);
                }

                mComments.postValue(currentList);
            }

            @Override
            public void onFailure(@NonNull Call<List<GistComment>> call, @NonNull Throwable t) {
                showError(t.getLocalizedMessage());
            }
        });
    }

    public boolean isMoreCommentsAvailable() {
        return mGistCommentPrevPage != 0;
    }

    public void createComment(String comment) {

        if (mGistId.isEmpty()) {
            return;
        }

        if (comment.trim().isEmpty()) {
            showError("You cannot create a blank comment");
            return;
        }

        mGitHubService.createCommentOnGist(mGistId, GistComment.fromString(comment)).enqueue(new Callback<GistComment>() {
            @Override
            public void onResponse(@NonNull Call<GistComment> call, @NonNull Response<GistComment> response) {
                if (!response.isSuccessful()) {
                    onResponseError(response);
                    return;
                }

                List<GistComment> comments = new ArrayList<>(mComments.getValue());
                comments.add(0, response.body());
                mComments.postValue(comments);
            }

            @Override
            public void onFailure(@NonNull Call<GistComment> call, @NonNull Throwable t) {
                showError(t.getLocalizedMessage());
            }
        });

    }

    private void getGistStar() {

        if (mGistId.isEmpty()) {
            return;
        }

        mGitHubService.getGistStarById(mGistId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.code() == 404) {
                    mIsGistStarred.postValue(false);
                    return;
                }
                if (response.code() == 204) {
                    mIsGistStarred.postValue(true);
                    return;
                }

                if (!response.isSuccessful()) {
                    onResponseError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull Throwable t) {
                showError(t.getLocalizedMessage());
            }
        });
    }

    public void starItemClicked() {
        if (mIsGistStarred == null) {
            return;
        }

        if (mIsGistStarred.getValue()) {
            unStarGist();
        }
        else {
            starGist();
        }
    }

    private void starGist() {

        if (mGistId.isEmpty()) {
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new ZeroContentLengthInterceptor())
                .addInterceptor(new BasicAuthInterceptor(mUsername, mToken))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_GITHUB)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        IGitHubService gitHubService = retrofit.create(IGitHubService.class);

        gitHubService.starGistById(mGistId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.code() == 204) {
                    mIsGistStarred.postValue(true);
                    return;
                }

                if (!response.isSuccessful()) {
                    onResponseError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                showError(t.getLocalizedMessage());
            }
        });
    }

    private void unStarGist() {

        if (mGistId.isEmpty()) {
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new ZeroContentLengthInterceptor())
                .addInterceptor(new BasicAuthInterceptor(mUsername, mToken))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_GITHUB)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        IGitHubService gitHubService = retrofit.create(IGitHubService.class);

        gitHubService.unstarGistById(mGistId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.code() == 204) {
                    mIsGistStarred.postValue(false);
                    return;
                }

                if (!response.isSuccessful()) {
                    onResponseError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                showError(t.getLocalizedMessage());
            }
        });
    }

    private void onResponseError(Response response) {

        if (response.code() == 400) {
            //HTTP 400 bad request
            // Possible problem with JSON body
            showError(response.message());

        }
        else if (response.code() == 401) {
            //HTTP 401 unauthorized
            // Login failed
            //  or
            // Need login first
            showError(response.message());
        }
        else if (response.code() == 403) {
            //HTTP 403 forbidden
            // Rate limit exceeded
            Headers headers = response.headers();
            Set<String> headerNames = headers.names();
            int rateLimit = 0;
            long rateLimitReset = 0;
            //int rateLimitRemaining = 0;
            for (String headerName: headerNames) {
                String headerValue = headers.get(headerName);
                if (headerValue == null) {
                    continue;
                }
                switch (headerName) {
                    case Constants.HEADER_RATELIMIT_LIMIT:
                        rateLimit = Integer.valueOf(headerValue);
                        break;
                    /*case "X-RateLimit-Remaining":
                        rateLimitRemaining = Integer.valueOf(headerValue);
                        break;*/
                    case Constants.HEADER_RATELIMIT_RESET:
                        rateLimitReset = Long.valueOf(headerValue);
                        break;
                }
            }

            if (rateLimit != 0) {
                Date resetDate = new Date(rateLimitReset * 1000);
                String resetTime = DateFormat.getTimeInstance().format(resetDate);
                String errorMessage = Constants.RATELIMIT_ERROR + resetTime;
                showError(errorMessage);
            }
        }
        else if (response.code() == 422) {
            //HTTP 422 un-processable Entity
            // Invalid field
            showError(response.message());
        }
        else {
            showError(response.message());
        }
    }

    private void showError(String message) {
        mProgressBarVisibility.postValue(View.GONE);
        mErrorMessage.postValue(message);
    }
}
