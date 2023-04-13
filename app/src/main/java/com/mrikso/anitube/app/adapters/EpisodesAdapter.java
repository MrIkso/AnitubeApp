package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.databinding.ItemEpisodeBinding;
import com.mrikso.anitube.app.model.EpisodeModel;

import java.util.ArrayList;
import java.util.List;

public class EpisodesAdapter extends RecyclerView.Adapter<EpisodesAdapter.ViewHolder> {
    private List<EpisodeModel> results = new ArrayList<>();

    private OnItemClickListener listener;
    private ItemEpisodeBinding binding;

    public void setResults(List<EpisodeModel> results) {
        this.results = results;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = ItemEpisodeBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EpisodeModel episode = results.get(position);
        holder.bind(episode);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemEpisodeBinding binding;

        public ViewHolder(@NonNull ItemEpisodeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(EpisodeModel episode) {
            binding.tvName.setText(episode.getName());
            if (listener != null) {
                binding.getRoot()
                        .setOnClickListener(
                                v -> listener.onEpisodeItemSelected(episode.getEpisodeUrl()));
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onEpisodeItemSelected(String url);
    }
}
