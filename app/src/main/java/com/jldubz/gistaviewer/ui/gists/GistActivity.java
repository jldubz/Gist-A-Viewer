package com.jldubz.gistaviewer.ui.gists;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.jldubz.gistaviewer.R;
import com.jldubz.gistaviewer.model.Constants;
import com.jldubz.gistaviewer.model.gists.Gist;
import com.jldubz.gistaviewer.model.gists.GistComment;
import com.jldubz.gistaviewer.ui.gists.comments.CommentAdapter;
import com.jldubz.gistaviewer.viewmodel.GistViewModel;

import java.text.DateFormat;
import java.util.List;

public class GistActivity extends AppCompatActivity {

    public static String KEY_GIST_ID = "com.jldubz.gistaviewer.ui.gists.GistActivity.gistId";

    private GistViewModel mViewModel;

    private ProgressBar mProgressBar;
    private TextView mFirstFilenameText;
    private TextView mDescriptionText;
    private TextView mFileCountText;
    private ImageView mAuthorAvatarImage;
    private TextView mAuthorNameText;
    private TextView mCreatedAtText;
    private TextView mLastUpdatedText;
    private Button mCreateCommentButton;
    private TextInputEditText mCommentInput;
    private RecyclerView mCommentList;
    private MenuItem mStarMenuItem;

    private CommentAdapter mCommentAdapter = new CommentAdapter();

    private boolean mIsLoadingMoreComments = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gist);

        //Configure toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_gist_top);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        }

        //Views
        mProgressBar = findViewById(R.id.progress_gist);
        mFirstFilenameText = findViewById(R.id.text_gist_filename);
        mDescriptionText = findViewById(R.id.text_gist_description);
        mFileCountText = findViewById(R.id.text_gist_files_count);
        mAuthorNameText = findViewById(R.id.text_gist_author);
        mAuthorAvatarImage = findViewById(R.id.image_gist_author_avatar);
        mCreatedAtText = findViewById(R.id.text_gist_created);
        mLastUpdatedText = findViewById(R.id.text_gist_updated);
        mCommentList = findViewById(R.id.list_gist_comments);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mCommentList.setLayoutManager(linearLayoutManager);
        mCommentAdapter.setIsLoadMoreEnabled(false);
        mCommentList.setAdapter(mCommentAdapter);

        mCommentList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView,
                                   int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!mCommentAdapter.isLoadMoreEnabled()) {
                    return;
                }

                int totalItemCount = linearLayoutManager.getItemCount();
                if (totalItemCount <= 0) {
                    return;
                }
                int lastVisibleItem = linearLayoutManager
                        .findLastVisibleItemPosition();
                if (!mIsLoadingMoreComments && lastVisibleItem >= totalItemCount - 1) {
                    mIsLoadingMoreComments = true;
                    mViewModel.loadMoreComments();
                }
            }
        });

        mCreateCommentButton = findViewById(R.id.button_gist_comments_create);
        mCreateCommentButton.setOnClickListener(this::onCreateCommentButtonClick);
        mCommentInput = findViewById(R.id.input_gist_comment);
        mCommentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                mCreateCommentButton.setEnabled(editable.length() > 0);
            }
        });

        //ViewModel
        mViewModel = ViewModelProviders.of(this).get(GistViewModel.class);

        //Gist ID
        Intent sourceIntent = getIntent();
        if (sourceIntent == null) {
            return;
        }

        String gistId = sourceIntent.getStringExtra(KEY_GIST_ID);
        if (gistId.isEmpty()) {
            onErrorChanged(Constants.INVALID_GIST_ID_ERROR);
        }
        else {
            mViewModel.setGistId(gistId);
        }

        //Saved credentials
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.key_pref_file), MODE_PRIVATE);
        String username = sharedPreferences.getString(getString(R.string.key_pref_username), "");
        String token = sharedPreferences.getString(getString(R.string.key_pref_token), "");

        if (!username.isEmpty() && !token.isEmpty()) {
            mViewModel.setCredentials(username, token);
        }

        observeViewModel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_gist_bottom, menu);
        mStarMenuItem = menu.findItem(R.id.menu_gist_star);
        mViewModel.getStarredState().observe(this, this::onStarStateChanged);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_gist_star:
                mViewModel.starItemClicked();
                return true;
            default:
                return false;
        }
    }

    private void observeViewModel() {
        mViewModel.getErrorMessage().observe(this, this::onErrorChanged);
        mViewModel.getGist().observe(this, this::onGistChanged);
        mViewModel.getComments().observe(this, this::onCommentsChanged);
        mViewModel.getProgressBarVisibility().observe(this, this::onProgressBarVisibilityChanged);
    }

    private void onErrorChanged(String message) {
        if (message == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error").setMessage(message).setPositiveButton("OK", null).show();
    }

    private void onGistChanged(Gist gist) {
        mFirstFilenameText.setText(gist.getFirstFile().getFilename());
        mDescriptionText.setText(gist.getDescription());
        mFileCountText.setText(String.valueOf(gist.getFiles().size()));
        mAuthorNameText.setText(gist.getOwner().getLogin());
        String updatedAtTime = DateFormat.getDateTimeInstance().format(gist.getUpdatedAt());
        String createdAtTime = DateFormat.getDateTimeInstance().format(gist.getCreatedAt());
        mCreatedAtText.setText(createdAtTime);
        mLastUpdatedText.setText(updatedAtTime);
        RequestOptions options = new RequestOptions().placeholder(R.drawable.ic_avatar_placeholder);
        Glide.with(this)
                .load(gist.getOwner().getAvatarUrl())
                .apply(options)
                .into(mAuthorAvatarImage);
    }

    private void onCommentsChanged(List<GistComment> comments) {
        mIsLoadingMoreComments = false;
        if (comments.size() == 0) {
            mCommentList.setVisibility(View.GONE);
        } else {
            mCommentList.setVisibility(View.VISIBLE);
        }
        mProgressBar.setVisibility(View.GONE);
        mCommentAdapter.setIsLoadMoreEnabled(mViewModel.isMoreCommentsAvailable());
        mCommentAdapter.setComments(comments);
    }

    private void onProgressBarVisibilityChanged(Integer visibility) {
        mProgressBar.setVisibility(visibility);
    }

    private void onCreateCommentButtonClick(View view) {
        mCommentInput.setError(null);
        Editable comment = mCommentInput.getText();
        if (comment == null) {
            mCommentInput.setError("You cannot create a blank comment");
            return;
        }

        //Hide the keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus == null) {
            currentFocus = new View(this);
        }
        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);

        mCommentInput.setText("");

        mViewModel.createComment(comment.toString());
    }

    private void onStarStateChanged(Boolean isStarred) {
        if (isStarred) {
            mStarMenuItem.setIcon(R.drawable.ic_star_filled);
        } else {
            mStarMenuItem.setIcon(R.drawable.ic_star_outline);
        }
    }
}
