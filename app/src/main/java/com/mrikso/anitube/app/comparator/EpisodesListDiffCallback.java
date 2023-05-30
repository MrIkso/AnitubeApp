package com.mrikso.anitube.app.comparator;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.anitube.app.parser.video.model.EpisodeModel;

import java.util.List;

public class EpisodesListDiffCallback extends DiffUtil.Callback {

    private List<EpisodeModel> oldData;
    private List<EpisodeModel> newData;

    public EpisodesListDiffCallback(List<EpisodeModel> oldData, List<EpisodeModel> newData) {
        this.oldData = oldData;
        this.newData = newData;
    }

    @Override
    public int getOldListSize() {
        if (oldData != null) {
            return oldData.size();
        }
        return 0;
    }

    @Override
    public int getNewListSize() {
        if (newData != null) {
            return newData.size();
        }
        return 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        EpisodeModel newItem = newData.get(newItemPosition);
        EpisodeModel oldItem = oldData.get(oldItemPosition);
        boolean result = oldItem.equals(newItem);
        System.out.println("areItemsTheSame: " + result + " old:" + oldItem + " new:" + newItem);
        return result;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        EpisodeModel newItem = newData.get(newItemPosition);
        EpisodeModel oldItem = oldData.get(oldItemPosition);
        boolean result = oldItem.equals(newItem);
        System.out.println("areContentsTheSame: " + result + " old:" + oldItem + " new:" + newItem);
        return result;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
