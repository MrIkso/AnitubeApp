package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.parser.video.model.EpisodeModel;

public class EpisodesDiffCallback extends DiffUtil.ItemCallback<EpisodeModel> {
    @Override
    public boolean areItemsTheSame(@NonNull EpisodeModel oldItem, @NonNull EpisodeModel newItem) {
        return oldItem.getEpisodeUrl().equals(newItem.getEpisodeUrl());
    }

    @Override
    public boolean areContentsTheSame(@NonNull EpisodeModel oldItem, @NonNull EpisodeModel newItem) {
        return oldItem.equals(newItem);
    }
}
