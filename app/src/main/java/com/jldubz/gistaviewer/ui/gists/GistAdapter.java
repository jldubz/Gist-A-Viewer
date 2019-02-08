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

/**
 * RecyclerView data adapter for displaying Gists
 *
 * @author Jon-Luke West
 */
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
        //Add one to the item count if loading more is enabled and there is already data in the list
        if (itemCount > 0 && mIsLoadMoreEnabled) {
            itemCount++;
        }
        return itemCount;
    }

    /***
     * Update the data set driving this adapter
     * @param gists the new list of gists to use
     */
    public void setGists(List<Gist> gists) {
        //When the new list is NULL, use a blank list
        if (gists == null) {
            mGists = new ArrayList<>();
            notifyDataSetChanged();
            return;
        }
        //Get the current size of the list
        int oldSize = mGists != null ? mGists.size() : 0;
        //Get the size of the new list
        int newSize = gists.size();
        //Update the gist list
        mGists = new ArrayList<>(gists);
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
     * Set or clear the interface listening to Gist clicks from this adapter
     * @param mListener the interface listening or NULL to clear it
     */
    void setListener(IGistListListener mListener) {
        this.mListener = mListener;
    }

    /***
     * Used to determine if loading more when the list is scrolled to the bottom is enabled
     * @return TRUE if loading more is enabled, FALSE if not
     */
    boolean isLoadMoreEnabled() {
        return mIsLoadMoreEnabled;
    }

    /***
     * Set whether loading more when the list is scrolled to the bottom is enabled or not
     * @param isLoadMoreEnabled TRUE if loading more is enabled, FALSE if not
     */
    void setIsLoadMoreEnabled(boolean isLoadMoreEnabled) {
        this.mIsLoadMoreEnabled = isLoadMoreEnabled;
    }

    @Override
    public void onGistClicked(int position) {

        if (mListener == null) {
            return;
        }

        //Tell the listener that a gist was clicked
        mListener.onGistClicked(mGists.get(position).getId());
    }

    public interface IGistListListener {

        /***
         * Called when a Gist is clicked
         * @param gistId the ID of the Gist that was clicked
         */
        void onGistClicked(String gistId);
    }
}
