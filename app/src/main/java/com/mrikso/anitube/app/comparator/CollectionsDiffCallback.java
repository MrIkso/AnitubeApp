package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.model.CollectionModel;

public class CollectionsDiffCallback extends DiffUtil.ItemCallback<CollectionModel> {
    @Override
    public boolean areItemsTheSame(@NonNull CollectionModel oldItem, @NonNull CollectionModel newItem) {
        return oldItem.getCollectionUrl() == newItem.getCollectionUrl();
    }

    @Override
    public boolean areContentsTheSame(@NonNull CollectionModel oldItem, @NonNull CollectionModel newItem) {
        return oldItem.getCollectionUrl() == newItem.getCollectionUrl();
    }
}
