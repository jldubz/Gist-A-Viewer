package com.jldubz.gistaviewer.ui.gists.comments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jldubz.gistaviewer.R;
import com.jldubz.gistaviewer.model.gists.GistComment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentAdapter extends RecyclerView.Adapter {

    private List<GistComment> mComments;

    @Override
    public int getItemViewType(int position) {

        return R.layout.item_comment;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);

        return new CommentViewHolder(view);

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

        return mComments.size();
    }

    public void setComments(List<GistComment> comments) {
        int oldSize = mComments != null ? mComments.size() : 0;
        int newSize = comments.size();
        if (newSize == oldSize+1) {
            notifyItemInserted(0);
            return;
        }
        mComments = new ArrayList<>(comments);
        notifyDataSetChanged();
    }
}
