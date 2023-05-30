package com.mrikso.anitube.app.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.common.base.Strings;
import com.mrikso.anitube.app.databinding.ItemAnimeReleaseBinding;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.WatchAnimeStatusModel;
import com.mrikso.anitube.app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

public class ReleaseAnimeAdapter extends RecyclerView.Adapter<ReleaseAnimeAdapter.ViewHolder> {
    private List<AnimeReleaseModel> results = new ArrayList<>();

    private OnItemClickListener listener;

    public void setResults(List<AnimeReleaseModel> results) {
        this.results.addAll(results);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAnimeReleaseBinding binding = ItemAnimeReleaseBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnimeReleaseModel episode = results.get(position);
        holder.bind(episode);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemAnimeReleaseBinding binding;

        public ViewHolder(@NonNull ItemAnimeReleaseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AnimeReleaseModel episode) {
            binding.title.setText(episode.getTitle());
            binding.episodes.setText(episode.getEpisodes());
            if (episode.getWatchStatusModdel() != null) {
                WatchAnimeStatusModel statusModel = episode.getWatchStatusModdel();

                binding.statusLayout.setVisibility(View.VISIBLE);
                binding.statusLayout.setBackgroundColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), statusModel.getColor()));
                binding.status.setText(statusModel.getStatus());
            } else {
                binding.statusLayout.setVisibility(View.GONE);
            }

            if (episode.getReleaseYear() != null) {
                binding.year.setText(episode.getReleaseYear().getText());
            }
            if (!Strings.isNullOrEmpty(episode.getRating())) {

                binding.llScore.setVisibility(View.VISIBLE);
                binding.rating.setRating(Float.parseFloat(episode.getRating()) / 2f);
                binding.tvScore.setText(String.format("%s/10", episode.getRating()));
            }
            binding.description.setText(episode.getDescription());

            Glide.with(binding.getRoot().getContext())
                    .load(ApiClient.BASE_URL + episode.getPosterUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(
                                @Nullable GlideException e,
                                Object model,
                                Target<Drawable> target,
                                boolean isFirstResource) {
                            binding.progressIndicator.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                Drawable resource,
                                Object model,
                                Target<Drawable> target,
                                DataSource dataSource,
                                boolean isFirstResource) {
                            binding.progressIndicator.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(binding.poster);

            if (listener != null) {
                binding.getRoot().setOnClickListener(v -> listener.onReleaseItemSelected(episode.getAnimeUrl()));
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onReleaseItemSelected(String link);
    }
}
