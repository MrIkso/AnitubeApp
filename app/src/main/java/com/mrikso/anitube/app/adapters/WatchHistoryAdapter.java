package com.mrikso.anitube.app.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.comparator.WatchHistoryDiffCallback;
import com.mrikso.anitube.app.data.history.enity.HistoryEnity;
import com.mrikso.anitube.app.databinding.ItemWatchHistoryBinding;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.ReadableTime;

public class WatchHistoryAdapter extends ListAdapter<HistoryEnity, WatchHistoryAdapter.ViewHolder> {

    private final RequestManager glide;
    private OnItemClickListener listener;
    private OnLongItemClickListener longItemClickListener;

    public WatchHistoryAdapter(RequestManager glide) {
        super(new WatchHistoryDiffCallback());
        this.glide = glide;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemWatchHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryEnity model = getItem(position);
        // Check for null
        if (model != null) {
            holder.bind(model);
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemWatchHistoryBinding binding;

        public ViewHolder(@NonNull ItemWatchHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(HistoryEnity model) {
            Context ctx = binding.getRoot().getContext();
            binding.title.setText(model.getName());
            binding.episode.setText(ctx.getString(R.string.episode_count, model.getEpisodeId()));
            binding.wathStatus.setText(
                    model.getTotalWatchTime() == model.getTotalEpisodeTime()
                            ? ctx.getString(R.string.watching_time_full)
                            : ctx.getString(
                                    R.string.watching_time, ReadableTime.generateTime(model.getTotalWatchTime())));
            binding.source.setText(model.getSourcePath());
            binding.time.setText(ReadableTime.getTimeAgo(
                    model.getWatchDate(), binding.getRoot().getContext()));

            DrawableCrossFadeFactory factory =
                    new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
            glide.load(ParserUtils.normaliseImageUrl(model.getPosterUrl()))
                    .transition(DrawableTransitionOptions.withCrossFade(factory))
                    .listener(new RequestListener<>() {
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
                binding.getRoot().setOnClickListener(v -> listener.onItemSelected(model.getAnimeUrl()));
            }

            if (longItemClickListener != null) {
                binding.getRoot().setOnLongClickListener(v -> {
                    longItemClickListener.onItemSelected(model.getAnimeId());
                    return true;
                });
            }
        }
    }

    public interface OnItemClickListener {
        void onItemSelected(String url);
    }

    public interface OnLongItemClickListener {
        void onItemSelected(int animeId);
    }

    public void setLongItemClickListener(OnLongItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }
}
