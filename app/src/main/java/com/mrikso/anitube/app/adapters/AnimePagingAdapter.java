package com.mrikso.anitube.app.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.common.base.Strings;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.databinding.ItemAnimeReleaseBinding;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.WatchAnimeStatusModel;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

public class AnimePagingAdapter extends PagingDataAdapter<AnimeReleaseModel, AnimePagingAdapter.ViewHolder> {
    public static final int LOADING_ITEM = 0;
    public static final int MOVIE_ITEM = 1;
    private final RequestManager glide;
    private OnItemClickListener listener;

    public AnimePagingAdapter(@NotNull DiffUtil.ItemCallback<AnimeReleaseModel> diffCallback, RequestManager glide) {
        super(diffCallback);
        this.glide = glide;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(ItemAnimeReleaseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnimeReleaseModel currentRelease = getItem(position);
        // Check for null
        if (currentRelease != null) {

            holder.bind(currentRelease);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // set ViewType
        return position == getItemCount() ? MOVIE_ITEM : LOADING_ITEM;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAnimeReleaseBinding binding;

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
            String rating = episode.getRating();
            if (!Strings.isNullOrEmpty(rating)) {

                binding.llScore.setVisibility(View.VISIBLE);
                binding.rating.setRating(Float.parseFloat(rating) / 2f);
                if (rating.endsWith(".0")) {
                    rating = StringUtils.removeChars(rating, 2);
                }
                binding.tvScore.setText(String.format("%s/10", rating));
            }
            binding.description.setText(episode.getDescription());

            DrawableCrossFadeFactory factory =
                    new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
            glide.load(ParserUtils.normaliseImageUrl(episode.getPosterUrl()))
                    .transition(DrawableTransitionOptions.withCrossFade(factory))
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(
                                @Nullable GlideException e,
                                Object model,
                                @NonNull Target<Drawable> target,
                                boolean isFirstResource) {
                            binding.progressIndicator.setVisibility(View.GONE);
                            binding.poster.setImageDrawable(AppCompatResources.getDrawable(binding.getRoot().getContext(), R.drawable.ic_broken_image));
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                @NonNull Drawable resource,
                                @NonNull Object model,
                                Target<Drawable> target,
                                @NonNull DataSource dataSource,
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
