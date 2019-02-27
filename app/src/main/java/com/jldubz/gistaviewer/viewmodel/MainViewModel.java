package com.jldubz.gistaviewer.viewmodel;

import android.view.View;

import com.jldubz.gistaviewer.model.Constants;
import com.jldubz.gistaviewer.model.NetworkUtil;
import com.jldubz.gistaviewer.model.data.BasicAuthInterceptor;
import com.jldubz.gistaviewer.model.gists.Gist;
import com.jldubz.gistaviewer.model.GitHubUser;
import com.jldubz.gistaviewer.model.data.IGitHubService;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ViewModel that handles business logic for Gist fragments DiscoverGistsFragment, StarGistsFragment, and YourGistsFragment and
 * the ProfileFragment
 *
 * @author Jon-Luke West
 */
public class MainViewModel extends ViewModel {

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

    private IMainViewModelListener mListener;

    private String mUsername;
    private String mToken;
    private int mStarredGistsPagesLoaded;
    private int mYourGistsPagesLoaded;
    private int mGistPagesLoaded = 0;
    private boolean mMoreDiscoveredGistsAvailable = true;
    private boolean mMoreYourGistsAvailable = true;
    private boolean mMoreStarredGistsAvailable = true;
    private boolean mIsLoggedIn;

    private IGitHubService mGitHubService;

    public MainViewModel() {
        super();
        init();
        initAnonService();
    }

    /***
     * Clean and initialize the state of the view and all related counters and flags
     */
    private void init() {

        showLoginForm();
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
    }

    /***
     * Configure a new Retrofit instance for future API calls with no authorization
     */
    private void initAnonService() {

        //Create an instance of the GitHub service interface using Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_GITHUB)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mGitHubService = retrofit.create(IGitHubService.class);
    }

    //region Profile

    /***
     * Attempt to login to GitHub using the provided username and token
     * @param username the GitHub username to authenticate as
     * @param token a private access token associated with the provided GitHub username
     */
    public void logIn(String username, String token) {

        //Clear the error state of the username and token fields
        mUsernameError.postValue(null);
        mTokenError.postValue(null);
        //Hide the login form
        mLoginFormVisibility.postValue(View.GONE);
        //Show the progress bar
        mProgressBarVisibility.postValue(View.VISIBLE);

        //Check to see if the username field is empty
        if (username.trim().isEmpty()) {
            //Hide the progress bar
            mProgressBarVisibility.postValue(View.GONE);
            //Show the login form
            mLoginFormVisibility.postValue(View.VISIBLE);
            //Set the error on the username field
            mUsernameError.postValue(Constants.USERNAME_ERROR);
            return;
        }

        //Check to see if the token field is empty
        if (token.trim().isEmpty()) {
            //Hide the progress bar
            mProgressBarVisibility.postValue(View.GONE);
            //Show the login form
            mLoginFormVisibility.postValue(View.VISIBLE);
            //Set the error on the token field
            mTokenError.postValue(Constants.TOKEN_ERROR);
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(username.trim(), token.trim()))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_GITHUB)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        mGitHubService = retrofit.create(IGitHubService.class);

        mGitHubService.getLoggedInUser().enqueue(new Callback<GitHubUser>() {
            @Override
            public void onResponse(@NonNull Call<GitHubUser> call, @NonNull Response<GitHubUser> response) {
                if (!response.isSuccessful()) {
                    showError(NetworkUtil.onGitHubResponseError(response));
                    showLoginForm();
                    return;
                }

                mUser.postValue(response.body());
                showProfile();
                saveCredentials(username, token);

                loadMoreYourGists();
                loadMoreStarredGists();
            }

            @Override
            public void onFailure(@NonNull Call<GitHubUser> call, @NonNull Throwable t) {
                showError(t.getLocalizedMessage());
            }
        });
    }

    /***
     * Logout of GitHub and reset the API service to anonymous access
     */
    public void logout() {
        init();
        initAnonService();
    }

    public LiveData<GitHubUser> getUser() {
        return mUser;
    }

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

    /**
     * Download public Gists that have been recently created or updated on GitHub
     */
    public void discoverMoreGists() {

        Call<List<Gist>> gists = mGitHubService.getPublicGists(mGistPagesLoaded + 1);
        gists.enqueue(new Callback<List<Gist>>() {
            @Override
            public void onResponse(@NonNull Call<List<Gist>> call, @NonNull Response<List<Gist>> response) {
                if (!response.isSuccessful()) {
                    showError(NetworkUtil.onGitHubResponseError(response));
                    return;
                }

                //increment the number of pages loaded
                mGistPagesLoaded++;
                //check to see if there is a "next" page
                mMoreDiscoveredGistsAvailable = isNextLinkAvailable(response);

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
            public void onFailure(Call<List<Gist>> call, Throwable t) {
                showError(t.getLocalizedMessage());
            }
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

    /**
     * Download Gists starred by the authorized user GitHub
     */
    public void loadMoreStarredGists() {

        mGitHubService.getStarredGists(mStarredGistsPagesLoaded + 1).enqueue(new Callback<List<Gist>>() {
            @Override
            public void onResponse(Call<List<Gist>> call, Response<List<Gist>> response) {
                if (!response.isSuccessful()) {
                    showError(NetworkUtil.onGitHubResponseError(response));
                    return;
                }

                //increment the number of pages loaded
                mStarredGistsPagesLoaded++;
                //check to see if there is a "next" page
                mMoreStarredGistsAvailable = isNextLinkAvailable(response);
                //update the list of starred Gists
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
            public void onFailure(Call<List<Gist>> call, Throwable t) {
                showError(t.getLocalizedMessage());
            }
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

    /**
     * Download Gists published by the authorized user GitHub
     */
    public void loadMoreYourGists() {
        //Make sure there is an authorized user; otherwise this call returns the same thing as discoverMoreGists();
        if (!mIsLoggedIn) {
            showError(Constants.NEED_LOGIN_ERROR);
            return;
        }

        mGitHubService.getYourGists(mYourGistsPagesLoaded + 1).enqueue(new Callback<List<Gist>>() {
            @Override
            public void onResponse(Call<List<Gist>> call, Response<List<Gist>> response) {
                if (!response.isSuccessful()) {
                    showError(NetworkUtil.onGitHubResponseError(response));
                    return;
                }

                //increment the number of pages loaded
                mYourGistsPagesLoaded++;
                //check to see if there is a "next" page
                mMoreYourGistsAvailable = isNextLinkAvailable(response);
                //update the list of your Gists
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
            public void onFailure(Call<List<Gist>> call, Throwable t) {
                showError(t.getLocalizedMessage());
            }
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

    /**
     * Set or clear the interface listening to calls to save credentials
     *
     * @param mListener the interface listening or NULL to clear it
     */
    public void setListener(IMainViewModelListener mListener) {
        this.mListener = mListener;
    }

    /**
     * Check a link header returned in a call to the GitHub API to see if there is a URL pointing to the next page of content
     *
     * @param response the Retrofit Response to process
     * @return TRUE is a next link was found, FALSE if not
     */
    private boolean isNextLinkAvailable(Response response) {

        String linkHeader = response.headers().get("Link");
        if (linkHeader == null) {
            return false;
        }

        int nextLinkIndex = linkHeader.indexOf("; rel=\"next\"");
        return nextLinkIndex >= 0;

    }

    /**
     * Convenience method for showing an error to the user
     *
     * @param message the message to show to the user
     */
    private void showError(String message) {
        mErrorMessage.postValue(message);
    }

    /**
     * Show the views for logging in and hide the profile views
     */
    private void showLoginForm() {
        mProfileVisibility.postValue(View.GONE);
        mLoginFormVisibility.postValue(View.VISIBLE);
        mLoginViewVisibility.postValue(View.VISIBLE);
        mProgressBarVisibility.postValue(View.GONE);
    }

    /**
     * Show the profile views and hide the login views
     */
    private void showProfile() {
        mProfileVisibility.postValue(View.VISIBLE);
        mLoginViewVisibility.postValue(View.GONE);
        mProgressBarVisibility.postValue(View.GONE);
    }

    /**
     * Trim and save credentials so that the login persists after the app closes
     * <p>
     * This assumes that there is an IMainViewModelListener configured
     *
     * @param username the username to save
     * @param token    the private access token to save
     */
    private void saveCredentials(String username, String token) {
        mIsLoggedIn = true;
        //trim white space
        mUsername = username.trim();
        mToken = token.trim();
        if (mListener != null) {
            mListener.saveCredentials(mUsername, mToken);
        }
    }

    public interface IMainViewModelListener {

        /**
         * Called when credentials need to be saved to Shared Preferences
         *
         * @param username the username to save
         * @param token    the private access token to save
         */
        void saveCredentials(String username, String token);
    }
}
