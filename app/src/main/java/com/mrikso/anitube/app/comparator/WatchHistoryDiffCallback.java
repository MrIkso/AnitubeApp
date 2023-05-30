package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.data.history.enity.HistoryEnity;

public class WatchHistoryDiffCallback extends DiffUtil.ItemCallback<HistoryEnity> {
    @Override
    public boolean areItemsTheSame(@NonNull HistoryEnity oldItem, @NonNull HistoryEnity newItem) {
        return oldItem.getAnimeId() == newItem.getAnimeId();
    }

    @Override
    public boolean areContentsTheSame(@NonNull HistoryEnity oldItem, @NonNull HistoryEnity newItem) {
        return oldItem.getAnimeId() == newItem.getAnimeId();
    }
}
