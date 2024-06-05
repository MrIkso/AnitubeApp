package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.model.AnimeReleaseModel;

public class AnimeReleaseComparator extends DiffUtil.ItemCallback<AnimeReleaseModel> {
    @Override
    public boolean areItemsTheSame(@NonNull AnimeReleaseModel oldItem, @NonNull AnimeReleaseModel newItem) {
        return oldItem.getAnimeId() == newItem.getAnimeId();
    }

    @Override
    public boolean areContentsTheSame(@NonNull AnimeReleaseModel oldItem, @NonNull AnimeReleaseModel newItem) {
        return oldItem.equals(newItem);
    }
}
