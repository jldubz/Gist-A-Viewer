package com.jldubz.gistaviewer.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.jldubz.gistaviewer.viewmodel.MainViewModel;
import com.jldubz.gistaviewer.R;
import com.jldubz.gistaviewer.model.GitHubUser;

import static android.content.Context.MODE_PRIVATE;

/**
 * Fragment used for showing a GitHub user and some of their basic info
 *
 * @author Jon-Luke West
 */
public class ProfileFragment extends Fragment implements MainViewModel.IMainViewModelListener {

    private MainViewModel mViewModel;

    private ImageView mAvatarImage;
    private TextView mUsernameText;
    private TextView mNameText;
    private TextView mFollowersCountText;
    private TextView mFollowingCountText;
    private TextView mPublicGistsCountText;
    private TextView mPrivateGistsCountText;
    private TextView mBioText;
    private TextView mCompanyText;
    private TextView mLocationText;
    private TextView mBlogText;

    private View mProfileView;
    private View mLoginView;
    private View mLoginFormView;
    private ProgressBar mLoginProgressBar;
    private TextInputEditText mUsernameInput;
    private TextInputEditText mTokenInput;

    static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        //Assign button click listeners
        Button whatTokenButton = rootView.findViewById(R.id.button_profile_login_what);
        whatTokenButton.setOnClickListener(v -> onWhatTokenButtonClick());
        Button loginButton = rootView.findViewById(R.id.button_profile_login);
        loginButton.setOnClickListener(v -> onLoginButtonClick());
        Button logoutButton = rootView.findViewById(R.id.button_profile_logout);
        logoutButton.setOnClickListener(v -> onLogoutButtonClick());

        //Login form
        mLoginFormView = rootView.findViewById(R.id.view_profile_login_form);
        mLoginView = rootView.findViewById(R.id.view_profile_login);
        mLoginProgressBar = rootView.findViewById(R.id.progress_profile);
        mUsernameInput = rootView.findViewById(R.id.input_profile_login_username);
        mTokenInput = rootView.findViewById(R.id.input_profile_login_token);

        //User profile view
        mProfileView = rootView.findViewById(R.id.view_profile);
        mAvatarImage = rootView.findViewById(R.id.image_profile_avatar);
        mUsernameText = rootView.findViewById(R.id.text_gist_filename);
        mNameText = rootView.findViewById(R.id.text_profile_name);
        mFollowersCountText = rootView.findViewById(R.id.text_gist_files_count);
        mFollowingCountText = rootView.findViewById(R.id.text_gist_forks_count);
        mPublicGistsCountText = rootView.findViewById(R.id.text_gist_comments_count);
        mPrivateGistsCountText = rootView.findViewById(R.id.text_gist_stars_count);
        mBioText = rootView.findViewById(R.id.text_profile_bio);
        mCompanyText = rootView.findViewById(R.id.text_profile_company);
        mLocationText = rootView.findViewById(R.id.text_profile_location);
        mBlogText = rootView.findViewById(R.id.text_profile_blog);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        mViewModel = ViewModelProviders.of(activity).get(MainViewModel.class);
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.setListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.setListener(null);
    }

    @Override
    public void saveCredentials(String username, String token) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        SharedPreferences sharedPreferences = activity.getApplicationContext().getSharedPreferences(getString(R.string.key_pref_file), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.key_pref_username), username);
        editor.putString(getString(R.string.key_pref_token), token);
        editor.apply();
    }

    /**
     * Called when the Login button was clicked by the user
     */
    private void onLoginButtonClick() {

        //Validate the input
        Editable username = mUsernameInput.getText();
        Editable token = mTokenInput.getText();
        if (username == null) {
            mUsernameInput.setError(getString(R.string.text_profile_username_error));
            return;
        }
        if (token == null) {
            mTokenInput.setError(getString(R.string.text_profile_token_error));
            return;
        }
        mViewModel.logIn(username.toString(), token.toString());

        //Hide the keyboard
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus == null) {
            currentFocus = new View(activity);
        }
        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }

    /**
     * Called when the Logout button was clicked by the user
     */
    private void onLogoutButtonClick() {
        mViewModel.logout();
        //Clear input fields
        mUsernameInput.setText("");
        mTokenInput.setText("");

        //Clear saved credentials
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        SharedPreferences sharedPreferences = activity.getApplicationContext().getSharedPreferences(getString(R.string.key_pref_file), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.key_pref_username), "");
        editor.putString(getString(R.string.key_pref_token), "");
        editor.apply();
    }

    /**
     * Called when the user clicks the "What is my private access token?" button
     */
    private void onWhatTokenButtonClick() {

        //Open related web post
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.url_github_about_tokens)));
        startActivity(i);
    }

    /**
     * Observe all of the necessary properties of the view model
     */
    private void observeViewModel() {
        mViewModel.setListener(this);
        mViewModel.getLoginFormVisibility().observe(this, visibility -> mLoginFormView.setVisibility(visibility));
        mViewModel.getLoginViewVisibility().observe(this, visibility -> mLoginView.setVisibility(visibility));
        mViewModel.getProfileVisibility().observe(this, visibility -> mProfileView.setVisibility(visibility));
        mViewModel.getProgressBarVisibility().observe(this, visibility -> mLoginProgressBar.setVisibility(visibility));
        mViewModel.getErrorMessage().observe(this, this::onErrorChanged);
        mViewModel.getUsernameError().observe(this, message -> mUsernameInput.setError(message));
        mViewModel.getTokenError().observe(this, message -> mTokenInput.setError(message));
        mViewModel.getUser().observe(this, this::onUserLoaded);
    }

    /**
     * Called when the user has been updated
     * @param user the new GitHub User data
     */
    private void onUserLoaded(GitHubUser user) {

        //Make sure we got a user
        if (user == null) {
            return;
        }

        //Set the login name
        mUsernameText.setText(user.getLogin());
        //Set the name
        mNameText.setText(user.getName());
        //Set the number of followers
        mFollowersCountText.setText(String.valueOf(user.getFollowers()));
        //Set the number of users this user is following
        mFollowingCountText.setText(String.valueOf(user.getFollowing()));
        //Set the number of public Gists published by this user
        mPublicGistsCountText.setText(String.valueOf(user.getPublicGists()));
        //Set the number of private Gists published by this user
        mPrivateGistsCountText.setText(String.valueOf(user.getPrivateGists()));
        //Set the bio text
        mBioText.setText(user.getBio());
        //Set the company
        mCompanyText.setText(user.getCompany());
        //Set the location
        mLocationText.setText(user.getLocation());
        //Set the blog link
        mBlogText.setText(user.getBlog());
        //Set the user's avatar image
        RequestOptions options = new RequestOptions().placeholder(R.drawable.ic_avatar_placeholder);
        Glide.with(this)
                .load(user.getAvatarUrl())
                .apply(options)
                .into(mAvatarImage);
    }

    /**
     * Called when a new error message is needs to be displayed to the user
     * @param message the error message to diaplsy
     */
    private void onErrorChanged(String message) {
        if (message == null) {
            return;
        }

        //Display error message
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getString(R.string.dialog_title_error))
                .setMessage(message)
                .setPositiveButton(getString(R.string.dialog_button_ok), null)
                .show();
    }
}
