package com.jldubz.gistaviewer.ui.gists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jldubz.gistaviewer.R;
import com.jldubz.gistaviewer.model.gists.Gist;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GistAdapter extends RecyclerView.Adapter implements GistViewHolder.IGistViewHolderListener {

    private List<Gist> mGists;
    private IGistListListener mListener;
    private boolean mIsLoadMoreEnabled = false;

    @Override
    public int getItemViewType(int position) {
        if (position >= mGists.size()) {
            return R.layout.item_load_more;
        }

        return R.layout.item_gist;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);

        switch (viewType) {
            case R.layout.item_gist:
                return new GistViewHolder(view, this);
            default:
                return new LoadMoreViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof GistViewHolder) {
            Gist gist = mGists.get(position);
            GistViewHolder gistViewHolder = (GistViewHolder) holder;
            gistViewHolder.configureView(gist);
        }
    }

    @Override
    public int getItemCount() {
        if (mGists == null) {
            return 0;
        }

        int itemCount = mGists.size();
        if (itemCount > 0 && mIsLoadMoreEnabled) {
            itemCount++;
        }
        return itemCount;
    }

    public void setGists(List<Gist> gists) {
        if (gists == null) {
            mGists = new ArrayList<>();
            notifyDataSetChanged();
            return;
        }
        int oldSize = mGists != null ? mGists.size() : 0;
        int newSize = gists.size();
        mGists = new ArrayList<>(gists);

        if (oldSize <= 0) {
            notifyDataSetChanged();
        }
        else if (newSize > oldSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        }
    }

    void setListener(IGistListListener mListener) {
        this.mListener = mListener;
    }

    boolean isLoadMoreEnabled() {
        return mIsLoadMoreEnabled;
    }

    void setIsLoadMoreEnabled(boolean isLoadMoreEnabled) {
        this.mIsLoadMoreEnabled = isLoadMoreEnabled;
    }

    @Override
    public void onGistClicked(int position) {

        if (mListener == null) {
            return;
        }

        mListener.onGistClicked(mGists.get(position).getId());
    }

    public interface IGistListListener {

        void onGistClicked(String gistId);
    }
}
