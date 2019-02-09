package com.jldubz.gistaviewer.ui.gists;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jldubz.gistaviewer.model.gists.Gist;

import java.util.List;

/**
 * Fragment used to display a list of Gists that have been starred by the app's authorized user
 *  on GitHub
 */
public class StarGistsFragment extends GistFragment {

    public static StarGistsFragment newInstance() {
        return new StarGistsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mGistList.getLayoutManager();

        if (linearLayoutManager != null) {

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
                        mViewModel.loadMoreStarredGists();
                    }
                }
            });
        }

        return rootView;
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();
        mViewModel.getStarredGists().observe(this, this::onGistsChanged);
    }

    @Override
    protected void onGistsChanged(List<Gist> gists) {
        mAdapter.setIsLoadMoreEnabled(mViewModel.isMoreStarredGistsAvailable());
        super.onGistsChanged(gists);
    }
}
