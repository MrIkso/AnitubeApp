package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.data.search.RecentSearch;

import java.util.Objects;

public class RecentSearchDiffCallback extends DiffUtil.ItemCallback<RecentSearch> {
    @Override
    public boolean areItemsTheSame(@NonNull RecentSearch oldItem, @NonNull RecentSearch newItem) {
        return Objects.equals(oldItem.getSearch_name(), newItem.getSearch_name());
    }

    @Override
    public boolean areContentsTheSame(@NonNull RecentSearch oldItem, @NonNull RecentSearch newItem) {
        return oldItem.equals(newItem);
    }
}
