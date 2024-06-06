package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.model.InteresingModel;

public class InteresingDiffCallback extends DiffUtil.ItemCallback<InteresingModel> {
    @Override
    public boolean areItemsTheSame(@NonNull InteresingModel oldItem, @NonNull InteresingModel newItem) {
        return oldItem.getAnimeUrl().equals(newItem.getAnimeUrl());
    }

    @Override
    public boolean areContentsTheSame(@NonNull InteresingModel oldItem, @NonNull InteresingModel newItem) {
        return oldItem.equals(newItem);
    }
}
