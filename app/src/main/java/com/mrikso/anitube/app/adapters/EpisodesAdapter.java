package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.comparator.EpisodesDiffCallback;
import com.mrikso.anitube.app.databinding.ItemEpisodeBinding;
import com.mrikso.anitube.app.parser.video.model.EpisodeModel;

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
        holder.bind(episode, position);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemEpisodeBinding binding;

        public ViewHolder(@NonNull ItemEpisodeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(EpisodeModel episode, int position) {
            binding.tvName.setText(episode.getName());
            if (listener != null) {
                binding.getRoot()
                        .setOnClickListener(
                                v ->
                                        listener.onEpisodeItemSelected(
                                                position, episode.getEpisodeUrl()));
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onEpisodeItemSelected(int episodeNumber, String url);
    }
}
