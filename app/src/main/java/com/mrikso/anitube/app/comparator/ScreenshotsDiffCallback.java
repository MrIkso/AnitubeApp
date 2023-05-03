package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.model.ScreenshotModel;

public class ScreenshotsDiffCallback extends DiffUtil.ItemCallback<ScreenshotModel> {
    @Override
    public boolean areItemsTheSame(
            @NonNull ScreenshotModel oldItem, @NonNull ScreenshotModel newItem) {
        return oldItem.getFullUrl() == newItem.getFullUrl();
    }

    @Override
    public boolean areContentsTheSame(
            @NonNull ScreenshotModel oldItem, @NonNull ScreenshotModel newItem) {
        return oldItem.getFullUrl() == newItem.getFullUrl();
    }
}
