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

/**
 * RecyclerView data adapter for displaying Gist Comments
 *
 * @author Jon-Luke West
 */
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
        //Add one to the item count if loading more is enabled and there is already data in the list
        if (itemCount > 0 && mIsLoadMoreEnabled) {
            itemCount++;
        }
        return itemCount;
    }

    /***
     * Update the data set driving this adapter
     * @param comments the new list of comments to use
     */
    public void setComments(List<GistComment> comments) {
        //When the new list is NULL, use a blank list
        if (comments == null) {
            mComments = new ArrayList<>();
            notifyDataSetChanged();
            return;
        }
        //Get the current size of the list
        int oldSize = mComments != null ? mComments.size() : 0;
        //Get the size of the new list
        int newSize = comments.size();
        //Update the comment list
        mComments = new ArrayList<>(comments);
        if (oldSize <= 0) {
            //When the size of the old list was 0, refresh the whole list
            notifyDataSetChanged();
        }
        else if (newSize > oldSize) {
            //When the size of the new list is greater than the old one, insert all new items
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        }
        else {
            notifyDataSetChanged();
        }
    }

    /***
     * Used to determine if loading more when the list is scrolled to the bottom is enabled
     * @return TRUE if loading more is enabled, FALSE if not
     */
    public boolean isLoadMoreEnabled() {
        return mIsLoadMoreEnabled;
    }

    /***
     * Set whether loading more when the list is scrolled to the bottom is enabled or not
     * @param isLoadMoreEnabled TRUE if loading more is enabled, FALSE if not
     */
    public void setIsLoadMoreEnabled(boolean isLoadMoreEnabled) {
        this.mIsLoadMoreEnabled = isLoadMoreEnabled;
    }
}
