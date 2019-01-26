package com.jldubz.gistaviewer.ui.gists;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jldubz.gistaviewer.R;
import com.jldubz.gistaviewer.model.gists.Gist;

import java.text.DateFormat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class GistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView mFileNameText;
    private TextView mUsernameText;
    ImageView mAvatarImage;
    private TextView mUpdatedText;

    private IGistViewHolderListener mListener;

    GistViewHolder(@NonNull View itemView, IGistViewHolderListener listener) {
        super(itemView);

        mListener = listener;

        mFileNameText = itemView.findViewById(R.id.text_gist_filename);
        mUsernameText = itemView.findViewById(R.id.text_comment_created);
        mAvatarImage = itemView.findViewById(R.id.image_gist_user_avatar);
        mUpdatedText = itemView.findViewById(R.id.text_gist_updated);
        itemView.setOnClickListener(this);
    }

    void configureView(Gist gist) {
        mFileNameText.setText(gist.getFirstFile().getFilename());
        mUsernameText.setText(gist.getOwner().getLogin());
        String updatedAtTime = DateFormat.getDateInstance().format(gist.getUpdatedAt());
        mUpdatedText.setText(updatedAtTime);
    }

    @Override
    public void onClick(View v) {
        if (mListener == null) {
            return;
        }

        mListener.onGistClicked(getAdapterPosition());
    }

    interface IGistViewHolderListener {

        void onGistClicked(int position);
    }
}
