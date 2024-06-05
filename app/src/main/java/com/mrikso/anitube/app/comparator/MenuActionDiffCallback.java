package com.mrikso.anitube.app.comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.model.MenuActionModel;

public class MenuActionDiffCallback extends DiffUtil.ItemCallback<MenuActionModel> {
    @Override
    public boolean areItemsTheSame(@NonNull MenuActionModel oldItem, @NonNull MenuActionModel newItem) {
        return oldItem.getId() == newItem.getId();
    }

    @Override
    public boolean areContentsTheSame(@NonNull MenuActionModel oldItem, @NonNull MenuActionModel newItem) {
        return oldItem.equals(newItem);
    }
}
