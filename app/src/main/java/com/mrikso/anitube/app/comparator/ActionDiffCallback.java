package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.model.ActionModel;

import java.util.Objects;

public class ActionDiffCallback extends DiffUtil.ItemCallback<ActionModel> {
    @Override
    public boolean areItemsTheSame(@NonNull ActionModel oldItem, @NonNull ActionModel newItem) {
        return oldItem.getType() == newItem.getType();
    }

    @Override
    public boolean areContentsTheSame(@NonNull ActionModel oldItem, @NonNull ActionModel newItem) {
        return oldItem.equals(newItem);
    }
}
