package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.mrikso.anitube.app.databinding.ItemAnimeReleaseBinding;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.network.ApiClient;

import org.jetbrains.annotations.NotNull;
import org.jsoup.internal.StringUtil;

public class MoviesAdapter extends PagingDataAdapter<AnimeReleaseModel, MoviesAdapter.ViewHolder> {
    // Define Loading ViewType
    public static final int LOADING_ITEM = 0;
    // Define Movie ViewType
    public static final int MOVIE_ITEM = 1;
    private final RequestManager glide;
    private OnItemClickListener listener;

    public MoviesAdapter(
            @NotNull DiffUtil.ItemCallback<AnimeReleaseModel> diffCallback, RequestManager glide) {
        super(diffCallback);
        this.glide = glide;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Return MovieViewHolder
        return new ViewHolder(
                ItemAnimeReleaseBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get current movie
        AnimeReleaseModel currentRelease = getItem(position);
        // Check for null
        if (currentRelease != null) {
            // Set Image of Movie using glide Library
            //    glide.load(ApiClient.BASE_URL + currentRelease.getPosterUrl())
            //          .into(holder.movieItemBinding.poster);

            // Set rating of movie
            holder.bind(currentRelease);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // set ViewType
        return position == getItemCount() ? MOVIE_ITEM : LOADING_ITEM;
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
            if (episode.getReleaseYear() != null) {
                binding.year.setText(episode.getReleaseYear().getText());
            }
            if (!StringUtil.isBlank(episode.getRating())) {

                binding.rating.setVisibility(View.VISIBLE);
                binding.rating.setText(episode.getRating());
            }
            binding.description.setText(episode.getDescription());

            glide.load(ApiClient.BASE_URL + episode.getPosterUrl()).into(binding.poster);

            if (listener != null) {
                binding.getRoot()
                        .setOnClickListener(
                                v -> listener.onReleaseItemSelected(episode.getAnimeUrl()));
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
