package com.mrikso.anitube.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.comparator.EpisodesDiffCallback;
import com.mrikso.anitube.app.databinding.ItemEpisodeBinding;
import com.mrikso.anitube.app.parser.video.model.EpisodeModel;
import com.mrikso.anitube.app.utils.ListUtils;
import com.mrikso.anitube.app.utils.ReadableTime;
import com.mrikso.anitube.app.utils.RecyclerAdapterHelper;

import java.util.ArrayList;
import java.util.List;

public class EpisodesAdapter extends ListAdapter<EpisodeModel, EpisodesAdapter.ViewHolder> {
    private OnItemClickListener listener;

    public EpisodesAdapter() {
        super(new EpisodesDiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemEpisodeBinding binding = ItemEpisodeBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EpisodeModel episode = getItem(position);
        if (episode != null) {
            holder.bind(episode, position);
        }
    }

    public int getOriginalPosition(int position) {
        return getCurrentList().size() - 1 - position;
    }

    /*public void setData(final List<EpisodeModel> newList) {
        RecyclerAdapterHelper.notifyChanges(this, currentList, newList);
        this.currentList = newList;
    }*/

    public void reverseList() {
        List<EpisodeModel> reversedList = ListUtils.reverseList(getCurrentList());
        submitList(reversedList);
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemEpisodeBinding binding;

        public ViewHolder(@NonNull ItemEpisodeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(EpisodeModel episode, int position) {
            binding.title.setText(episode.getName());
            // Log.i("epadapter", episode.toString());
            Context ctx = binding.getRoot().getContext();
            binding.watchedLayout.setVisibility(episode.isWatched() ? View.VISIBLE : View.GONE);

            if (episode.getTotalEpisodeTime() != 0) binding.summary.setVisibility(View.VISIBLE);
            binding.summary.setText(
                    episode.getTotalWatchTime() == episode.getTotalEpisodeTime()
                            ? ctx.getString(R.string.watching_time_full)
                            : ctx.getString(
                                    R.string.watching_time, ReadableTime.generateTime(episode.getTotalWatchTime())));

            if (listener != null) {
                binding.getRoot()
                        .setOnClickListener(v -> listener.onEpisodeItemSelected(position, episode.getEpisodeUrl()));
                binding.download.setOnClickListener(v -> listener.onEpisodeDownload(position, episode.getEpisodeUrl()));
                binding.getRoot().setOnLongClickListener(v -> {
                    listener.onEpisodeItemLongClicked(position, episode.getEpisodeUrl());
                    return true;
                });
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onEpisodeItemSelected(int episodeNumber, String url);
        void onEpisodeDownload(int episodeNumber, String url);

        void onEpisodeItemLongClicked(int episodeNumber, String url);
    }
}
