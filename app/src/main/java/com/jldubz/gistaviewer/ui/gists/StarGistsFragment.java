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

public class StarGistsFragment extends Fragment implements GistAdapter.IGistListListener {

    private MainViewModel mViewModel;

    private View mEmptyListView;
    private ProgressBar mProgressBar;
    private RecyclerView mGistList;

    private GistAdapter mAdapter = new GistAdapter();

    private boolean mIsLoadingMore = false;

    public static StarGistsFragment newInstance() {
        return new StarGistsFragment();
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

        mGistList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView,
                                   int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!mAdapter.isLoadMoreEnabled()) {
                    return;
                }

                int totalItemCount = linearLayoutManager.getItemCount();
                if (totalItemCount <= 0) {
                    return;
                }
                int lastVisibleItem = linearLayoutManager
                        .findLastVisibleItemPosition();
                if (!mIsLoadingMore && lastVisibleItem >= totalItemCount - 1) {
                    mIsLoadingMore = true;
                    mViewModel.loadMoreStarredGists();
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

    private void observeViewModel() {

        mViewModel.getErrorMessage().observe(this, this::onErrorChanged);
        mViewModel.getStarredGists().observe(this, this::onStarredGistsChanged);
    }

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

    private void onStarredGistsChanged(List<Gist> gists) {
        if (gists == null || gists.size() == 0) {
            mAdapter.setGists(gists);
            mEmptyListView.setVisibility(View.VISIBLE);
            mGistList.setVisibility(View.GONE);
        } else {
            mGistList.setVisibility(View.VISIBLE);
            mEmptyListView.setVisibility(View.GONE);
        }
        mAdapter.setIsLoadMoreEnabled(mViewModel.isMoreStarredGistsAvailable());
        mAdapter.setGists(gists);
        mProgressBar.setVisibility(View.GONE);
        mIsLoadingMore = false;
    }
}
