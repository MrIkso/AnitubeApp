package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.model.TorrentModel;

import java.util.Objects;

public class TorrentModelDiffCallback extends DiffUtil.ItemCallback<TorrentModel> {
    @Override
    public boolean areItemsTheSame(@NonNull TorrentModel oldItem, @NonNull TorrentModel newItem) {
        return Objects.equals(oldItem.getTorrentUrl(), newItem.getTorrentUrl());
    }

    @Override
    public boolean areContentsTheSame(@NonNull TorrentModel oldItem, @NonNull TorrentModel newItem) {
        return oldItem.equals(newItem);
    }
}
