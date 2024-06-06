package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.model.BaseAnimeModel;

public class BaseAnimeDiffCallback extends DiffUtil.ItemCallback<BaseAnimeModel> {
    @Override
    public boolean areItemsTheSame(@NonNull BaseAnimeModel oldItem, @NonNull BaseAnimeModel newItem) {
        return oldItem.getAnimeUrl().equals(newItem.getAnimeUrl());
    }

    @Override
    public boolean areContentsTheSame(@NonNull BaseAnimeModel oldItem, @NonNull BaseAnimeModel newItem) {
        return oldItem.equals(newItem);
    }
}
