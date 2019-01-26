package com.jldubz.gistaviewer.ui.gists.comments;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jldubz.gistaviewer.R;
import com.jldubz.gistaviewer.model.gists.GistComment;

import java.text.DateFormat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    public ImageView mAuthorAvatarImage;
    private TextView mAuthorNameText;
    private TextView mCreatedText;
    private TextView mCommentText;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);

        mAuthorAvatarImage = itemView.findViewById(R.id.image_comment_user_avatar);
        mAuthorNameText = itemView.findViewById(R.id.text_comment_username);
        mCreatedText = itemView.findViewById(R.id.text_comment_created);
        mCommentText = itemView.findViewById(R.id.text_comment);
    }

    public void configureView(GistComment comment) {

        mAuthorNameText.setText(comment.getUser().getLogin());
        String createdOnTime = DateFormat.getDateTimeInstance().format(comment.getCreatedAt());
        mCreatedText.setText(createdOnTime);
        mCommentText.setText(comment.getBody());
    }

}
