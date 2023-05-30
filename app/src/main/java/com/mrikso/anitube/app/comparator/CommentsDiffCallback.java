package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.model.CommentModel;

public class CommentsDiffCallback extends DiffUtil.ItemCallback<CommentModel> {
    @Override
    public boolean areItemsTheSame(@NonNull CommentModel oldItem, @NonNull CommentModel newItem) {
        return oldItem.getCommentId() == newItem.getCommentId();
    }

    @Override
    public boolean areContentsTheSame(@NonNull CommentModel oldItem, @NonNull CommentModel newItem) {
        return oldItem.getCommentId() == newItem.getCommentId();
    }
}
