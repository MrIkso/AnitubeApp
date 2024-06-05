package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.model.FranchiseModel;

public class FranchiseDiffCallback extends DiffUtil.ItemCallback<FranchiseModel> {
    @Override
    public boolean areItemsTheSame(@NonNull FranchiseModel oldItem, @NonNull FranchiseModel newItem) {
        return oldItem.getAnimeUrl().equals(newItem.getAnimeUrl());
    }

    @Override
    public boolean areContentsTheSame(@NonNull FranchiseModel oldItem, @NonNull FranchiseModel newItem) {
        return oldItem.equals(newItem);
    }
}
