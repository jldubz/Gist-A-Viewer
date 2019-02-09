package com.jldubz.gistaviewer.ui.gists;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.jldubz.gistaviewer.R;
import com.jldubz.gistaviewer.model.gists.Gist;
import com.jldubz.gistaviewer.viewmodel.MainViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GistFragment extends Fragment implements GistAdapter.IGistListListener {

    MainViewModel mViewModel;

    RecyclerView mGistList;
    GistAdapter mAdapter = new GistAdapter();

    boolean mIsLoadingMore = false;

    private View mEmptyListView;
    private ProgressBar mProgressBar;

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
        mAdapter.setIsLoadMoreEnabled(true);
        mGistList.setAdapter(mAdapter);

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
    protected void observeViewModel() {
        mViewModel.getErrorMessage().observe(this, this::onErrorChanged);
    }

    /**
     * Called when the list of Gists has updated and the UI needs to be updated to reflect it
     * @param gists the new list of Gists
     */
    protected void onGistsChanged(List<Gist> gists) {
        if (gists.size() == 0) {
            mEmptyListView.setVisibility(View.VISIBLE);
            mGistList.setVisibility(View.GONE);
        }
        else {
            mGistList.setVisibility(View.VISIBLE);
            mEmptyListView.setVisibility(View.GONE);
        }
        mProgressBar.setVisibility(View.GONE);
        mAdapter.setGists(gists);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Error").setMessage(message).setPositiveButton("OK", null).show();
    }
}
