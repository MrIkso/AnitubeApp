package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.model.SimpleModel;

public class SimpleModelDiffCallback extends DiffUtil.ItemCallback<SimpleModel> {
    @Override
    public boolean areItemsTheSame(@NonNull SimpleModel oldItem, @NonNull SimpleModel newItem) {
        return oldItem.getUrl() == newItem.getUrl();
    }

    @Override
    public boolean areContentsTheSame(@NonNull SimpleModel oldItem, @NonNull SimpleModel newItem) {
        return oldItem.getUrl() == newItem.getUrl();
    }
}
