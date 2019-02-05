package com.jldubz.gistaviewer.viewmodel;

import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jldubz.gistaviewer.R;
import com.jldubz.gistaviewer.model.Constants;
import com.jldubz.gistaviewer.model.data.BasicAuthInterceptor;
import com.jldubz.gistaviewer.model.gists.Gist;
import com.jldubz.gistaviewer.model.GitHubUser;
import com.jldubz.gistaviewer.model.data.IGitHubService;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainViewModel extends ViewModel {

    private boolean mIsLoggedIn;

    private MutableLiveData<GitHubUser> mUser = new MutableLiveData<>();
    private MutableLiveData<List<Gist>> mStarredGists = new MutableLiveData<>();
    private MutableLiveData<List<Gist>> mYourGists = new MutableLiveData<>();
    private MutableLiveData<List<Gist>> mDiscoveredGists = new MutableLiveData<>();

    private MutableLiveData<Integer> mLoginViewVisibility = new MutableLiveData<>();
    private MutableLiveData<Integer> mLoginFormVisibility = new MutableLiveData<>();
    private MutableLiveData<Integer> mProfileVisibility = new MutableLiveData<>();
    private MutableLiveData<Integer> mProgressBarVisibility = new MutableLiveData<>();

    private MutableLiveData<String> mErrorMessage = new MutableLiveData<>();
    private MutableLiveData<String> mUsernameError = new MutableLiveData<>();
    private MutableLiveData<String> mTokenError = new MutableLiveData<>();

    private IGitHubService mGitHubService;
    private IMainViewModelListener mListener;

    private int mStarredGistsPagesLoaded;
    private int mYourGistsPagesLoaded;
    private int mGistPagesLoaded;
    private String mUsername;
    private String mToken;
    private boolean mMoreDiscoveredGistsAvailable = true;
    private boolean mMoreYourGistsAvailable = true;
    private boolean mMoreStarredGistsAvailable = true;

    public MainViewModel() {
        super();
        initAnonService();
    }

    private void initAnonService() {
        mLoginViewVisibility.setValue(View.VISIBLE);
        mLoginFormVisibility.setValue(View.VISIBLE);
        mProfileVisibility.setValue(View.GONE);
        mProgressBarVisibility.setValue(View.GONE);
        mErrorMessage.setValue(null);
        mUsernameError.setValue(null);
        mTokenError.setValue(null);
        mUser.postValue(null);
        mStarredGists.postValue(null);
        mYourGists.postValue(null);
        mUsername = null;
        mToken = null;
        mIsLoggedIn = false;
        mStarredGistsPagesLoaded = 0;
        mYourGistsPagesLoaded = 0;

        Gson gson = new GsonBuilder()
                .setDateFormat(R.string.date_format)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_GITHUB)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        mGitHubService = retrofit.create(IGitHubService.class);
    }

    //region Profile

    public void logIn(String username, String token) {

        mUsernameError.postValue(null);
        mTokenError.postValue(null);
        mLoginFormVisibility.postValue(View.GONE);
        mProgressBarVisibility.postValue(View.VISIBLE);

        if (username.trim().isEmpty()) {
            mProgressBarVisibility.postValue(View.GONE);
            mLoginFormVisibility.postValue(View.VISIBLE);
            mUsernameError.postValue(Constants.USERNAME_ERROR);
            return;
        }

        if (token.trim().isEmpty()) {
            mProgressBarVisibility.postValue(View.GONE);
            mLoginFormVisibility.postValue(View.VISIBLE);
            mTokenError.postValue(Constants.TOKEN_ERROR);
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(username.trim(), token.trim()))
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

        mGitHubService.getLoggedInUser().enqueue(new Callback<GitHubUser>() {
            @Override
            public void onResponse(@NonNull Call<GitHubUser> call, @NonNull Response<GitHubUser> response) {
                if (!response.isSuccessful()) {
                    onResponseError(response);
                    mProfileVisibility.postValue(View.GONE);
                    mLoginFormVisibility.postValue(View.VISIBLE);
                    mLoginViewVisibility.postValue(View.VISIBLE);
                    mProgressBarVisibility.postValue(View.GONE);
                    return;
                }

                mProfileVisibility.postValue(View.VISIBLE);
                mLoginViewVisibility.postValue(View.GONE);
                mUser.postValue(response.body());
                mIsLoggedIn = true;
                mUsername = username.trim();
                mToken = token.trim();
                if (mListener != null) {
                    mListener.saveCredentials(mUsername, mToken);
                }

                loadMoreYourGists();
                loadMoreStarredGists();
            }

            @Override
            public void onFailure(@NonNull Call<GitHubUser> call, @NonNull Throwable t) {

            }
        });
    }

    public void logout() {
        initAnonService();
    }

    public LiveData<GitHubUser> getUser() { return mUser; }

    public LiveData<Integer> getLoginViewVisibility() {
        return mLoginViewVisibility;
    }

    public LiveData<Integer> getLoginFormVisibility() {
        return mLoginFormVisibility;
    }

    public LiveData<Integer> getProfileVisibility() {
        return mProfileVisibility;
    }

    public LiveData<Integer> getProgressBarVisibility() {
        return mProgressBarVisibility;
    }

    public LiveData<String> getUsernameError() {
        return mUsernameError;
    }

    public LiveData<String> getTokenError() {
        return mTokenError;
    }

    //endregion

    //region Discover Gists

    public void discoverMoreGists() {

        Call<List<Gist>> gists = mGitHubService.getPublicGists(mGistPagesLoaded+1);
        gists.enqueue(new Callback<List<Gist>>() {
            @Override
            public void onResponse(@NonNull Call<List<Gist>> call, @NonNull Response<List<Gist>> response) {
                if (!response.isSuccessful()) {
                    onResponseError(response);
                    return;
                }

                //increment the number of pages loaded
                mGistPagesLoaded++;
                //check to see if there is a "next" page
                String linkHeader = response.headers().get("Link");
                if (linkHeader != null) {
                    int nextLinkIndex = linkHeader.indexOf("; rel=\"next\"");
                    mMoreDiscoveredGistsAvailable = nextLinkIndex >= 0;
                }
                else {
                    mMoreDiscoveredGistsAvailable = false;
                }

                //update the list of discovered gists
                List<Gist> currentList = mDiscoveredGists.getValue();
                if (currentList == null) {
                    currentList = new ArrayList<>();
                }
                if (response.body() != null) {
                    currentList.addAll(response.body());
                }
                mDiscoveredGists.postValue(currentList);
            }

            @Override
            public void onFailure(Call<List<Gist>> call, Throwable t) { }
        });
    }

    public LiveData<List<Gist>> getDiscoveredGists() {
        if (mGistPagesLoaded == 0) {
            discoverMoreGists();
        }

        return mDiscoveredGists;
    }

    public boolean isMoreDiscoveredGistsAvailable() {
        return mMoreDiscoveredGistsAvailable;
    }

    //endregion

    //region Starred Gists

    public void loadMoreStarredGists() {
        if (!mIsLoggedIn) {
            showError(Constants.NEED_LOGIN_ERROR);
            return;
        }

        mGitHubService.getStarredGists(mStarredGistsPagesLoaded+1).enqueue(new Callback<List<Gist>>() {
            @Override
            public void onResponse(Call<List<Gist>> call, Response<List<Gist>> response) {
                if (!response.isSuccessful()) {
                    onResponseError(response);
                    return;
                }

                //increment the number of pages loaded
                mStarredGistsPagesLoaded++;
                String linkHeader = response.headers().get("Link");
                if (linkHeader != null) {
                    int nextLinkIndex = linkHeader.indexOf("; rel=\"next\"");
                    mMoreStarredGistsAvailable = nextLinkIndex >= 0;
                }
                else {
                    mMoreStarredGistsAvailable = false;
                }

                List<Gist> currentList = mStarredGists.getValue();
                if (currentList == null) {
                    currentList = new ArrayList<>();
                }
                if (response.body() != null) {
                    currentList.addAll(response.body());
                }

                mStarredGists.postValue(currentList);
            }

            @Override
            public void onFailure(Call<List<Gist>> call, Throwable t) { }
        });
    }

    public LiveData<List<Gist>> getStarredGists() {
        if (mStarredGistsPagesLoaded == 0) {
            loadMoreStarredGists();
        }

        return mStarredGists;
    }

    public boolean isMoreStarredGistsAvailable() {
        return mMoreStarredGistsAvailable;
    }

    //endregion

    //region Your Gists

    public void loadMoreYourGists() {
        if (!mIsLoggedIn) {
            showError(Constants.NEED_LOGIN_ERROR);
            return;
        }

        mGitHubService.getYourGists(mYourGistsPagesLoaded+1).enqueue(new Callback<List<Gist>>() {
            @Override
            public void onResponse(Call<List<Gist>> call, Response<List<Gist>> response) {
                if (!response.isSuccessful()) {
                    onResponseError(response);
                    return;
                }

                mYourGistsPagesLoaded++;

                String linkHeader = response.headers().get("Link");
                if (linkHeader != null) {
                    int nextLinkIndex = linkHeader.indexOf("; rel=\"next\"");
                    mMoreYourGistsAvailable = nextLinkIndex >= 0;
                }
                else {
                    mMoreYourGistsAvailable = false;
                }

                List<Gist> currentList = mYourGists.getValue();
                if (currentList == null) {
                    currentList = new ArrayList<>();
                }
                if (response.body() != null) {
                    currentList.addAll(response.body());
                }

                mYourGists.postValue(currentList);
            }

            @Override
            public void onFailure(Call<List<Gist>> call, Throwable t) { }
        });
    }

    public LiveData<List<Gist>> getYourGists() {
        if (mYourGistsPagesLoaded == 0) {
            loadMoreYourGists();
        }

        return mYourGists;
    }

    public boolean isMoreYourGistsAvailable() {
        return mMoreYourGistsAvailable;
    }

    //endregion

    public LiveData<String> getErrorMessage() {
        mErrorMessage.setValue(null);
        return mErrorMessage;
    }

    public void setListener(IMainViewModelListener mListener) {
        this.mListener = mListener;
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
        mErrorMessage.postValue(message);
    }

    public interface IMainViewModelListener {

        void saveCredentials(String username, String token);
    }
}
