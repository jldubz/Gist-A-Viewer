package com.jldubz.gistaviewer.ui.gists.comments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jldubz.gistaviewer.R;
import com.jldubz.gistaviewer.model.gists.GistComment;
import com.jldubz.gistaviewer.ui.gists.LoadMoreViewHolder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentAdapter extends RecyclerView.Adapter {

    private List<GistComment> mComments;
    private boolean mIsLoadMoreEnabled = false;

    @Override
    public int getItemViewType(int position) {

        if (position >= mComments.size()) {
            return R.layout.item_load_more;
        }
        return R.layout.item_comment;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);

        switch (viewType) {
            case R.layout.item_comment:
                return new CommentViewHolder(view);
            default:
                return new LoadMoreViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof CommentViewHolder) {
            GistComment gistComment = mComments.get(position);
            CommentViewHolder commentViewHolder = (CommentViewHolder) holder;
            commentViewHolder.configureView(gistComment);
        }
    }

    @Override
    public int getItemCount() {
        if (mComments == null) {
            return 0;
        }

        int itemCount = mComments.size();
        if (itemCount > 0 && mIsLoadMoreEnabled) {
            itemCount++;
        }
        return itemCount;
    }

    public void setComments(List<GistComment> comments) {
        if (comments == null) {
            mComments = new ArrayList<>();
            notifyDataSetChanged();
            return;
        }
        int oldSize = mComments != null ? mComments.size() : 0;
        int newSize = comments.size();
        mComments = new ArrayList<>(comments);
        if (oldSize <= 0) {
            notifyDataSetChanged();
        }
        else if (newSize == oldSize+1) {
            notifyItemInserted(0);
        }
        else if (newSize > oldSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        }
    }

    public boolean isLoadMoreEnabled() {
        return mIsLoadMoreEnabled;
    }

    public void setIsLoadMoreEnabled(boolean isLoadMoreEnabled) {
        this.mIsLoadMoreEnabled = isLoadMoreEnabled;
    }
}
