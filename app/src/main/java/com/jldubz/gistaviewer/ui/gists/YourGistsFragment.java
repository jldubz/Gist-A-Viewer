package com.jldubz.gistaviewer.ui.gists;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.jldubz.gistaviewer.R;
import com.jldubz.gistaviewer.model.gists.Gist;
import com.jldubz.gistaviewer.viewmodel.MainViewModel;

import java.util.List;

/**
 * Fragment used to display a list of Gists that have been published by the app's authorized user
 *  on GitHub
 */
public class YourGistsFragment extends Fragment implements GistAdapter.IGistListListener {

    private MainViewModel mViewModel;

    private View mEmptyListView;
    private ProgressBar mProgressBar;
    private RecyclerView mGistList;

    private GistAdapter mAdapter = new GistAdapter();

    private boolean mIsLoadingMore = false;

    public static YourGistsFragment newInstance() {
        return new YourGistsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gists, container, false);

        mEmptyListView = rootView.findViewById(R.id.view_gists_empty);
        mProgressBar = rootView.findViewById(R.id.progress_gists);
        mGistList = rootView.findViewById(R.id.list_gists);
        mGistList.setVisibility(View.GONE);

        FragmentActivity activity = getActivity();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);
        mGistList.setLayoutManager(linearLayoutManager);
        mGistList.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        mGistList.setAdapter(mAdapter);

        //Add a scroll listener to trigger a call to load more when the user reaches the bottom
        // of the list
        mGistList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView,
                                   int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //Prevent any calls to update when the list has it disabled
                if (!mAdapter.isLoadMoreEnabled()) {
                    return;
                }

                //Prevent any calls to update if the list is empty
                int totalItemCount = linearLayoutManager.getItemCount();
                if (totalItemCount <= 0) {
                    return;
                }
                //Check to see if the last visible item is the last item in the list
                int lastVisibleItem = linearLayoutManager
                        .findLastVisibleItemPosition();
                if (!mIsLoadingMore && lastVisibleItem >= totalItemCount - 1) {
                    mIsLoadingMore = true;
                    mViewModel.loadMoreYourGists();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.setListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.setListener(null);
    }

    @Override
    public void onGistClicked(String gistId) {
        Intent gistIntent = new Intent(getActivity(), GistActivity.class);
        gistIntent.putExtra(GistActivity.KEY_GIST_ID, gistId);
        startActivity(gistIntent);
    }

    /**
     * Observe all of the necessary properties of the view model
     */
    private void observeViewModel() {
        mViewModel.getErrorMessage().observe(this, this::onErrorChanged);
        mViewModel.getYourGists().observe(this, this::onYourGistsChanged);
    }

    /**
     * Called when the list of Gists has updated and the UI needs to be updated to reflect it
     * @param gists the new list of Gists
     */
    private void onYourGistsChanged(List<Gist> gists) {
        if (gists == null || gists.size() == 0) {
            mEmptyListView.setVisibility(View.VISIBLE);
            mGistList.setVisibility(View.GONE);
        } else {
            mGistList.setVisibility(View.VISIBLE);
            mEmptyListView.setVisibility(View.GONE);
        }
        mAdapter.setIsLoadMoreEnabled(mViewModel.isMoreYourGistsAvailable());
        mAdapter.setGists(gists);
        mProgressBar.setVisibility(View.GONE);
        mIsLoadingMore = false;
    }

    /**
     * Called when a new error message is needs to be displayed to the user
     * @param message the error message to diaplsy
     */
    private void onErrorChanged(String message) {
        if (message == null) {
            return;
        }
        mProgressBar.setVisibility(View.GONE);
        if (mAdapter.getItemCount() == 0) {
            mEmptyListView.setVisibility(View.VISIBLE);
            mGistList.setVisibility(View.GONE);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Error").setMessage(message).setPositiveButton("OK", null).show();
    }
}
