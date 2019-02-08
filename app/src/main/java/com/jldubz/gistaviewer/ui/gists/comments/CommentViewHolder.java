package com.jldubz.gistaviewer.ui.gists.comments;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jldubz.gistaviewer.R;
import com.jldubz.gistaviewer.model.gists.GistComment;

import java.text.DateFormat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Displays a GitHub Gist Comment in a RecyclerView
 *
 * @author Jon-Luke West
 */
class CommentViewHolder extends RecyclerView.ViewHolder {

    private ImageView mAuthorAvatarImage;
    private TextView mAuthorNameText;
    private TextView mCreatedText;
    private TextView mCommentText;

    CommentViewHolder(@NonNull View itemView) {
        super(itemView);

        mAuthorAvatarImage = itemView.findViewById(R.id.image_comment_user_avatar);
        mAuthorNameText = itemView.findViewById(R.id.text_comment_username);
        mCreatedText = itemView.findViewById(R.id.text_comment_created);
        mCommentText = itemView.findViewById(R.id.text_comment);
    }

    /***
     * Configure the view according to the Gist Comment info provided
     * @param comment the Gist Comment info to display in this view
     */
    void configureView(GistComment comment) {

        //Set the author login name
        mAuthorNameText.setText(comment.getUser().getLogin());
        //Set the created time
        String createdOnTime = DateFormat.getDateTimeInstance().format(comment.getCreatedAt());
        mCreatedText.setText(createdOnTime);
        //Set the comment body
        mCommentText.setText(comment.getBody());
        //Set the author avatar image
        RequestOptions options = new RequestOptions().placeholder(R.drawable.ic_avatar_placeholder);
        Glide.with(mAuthorAvatarImage)
                .load(comment.getUser().getAvatarUrl())
                .apply(options)
                .into(mAuthorAvatarImage);
    }

}
