package com.mrikso.anitube.app.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.comparator.ScreenshotsDiffCallback;
import com.mrikso.anitube.app.databinding.ItemScreenshotBinding;
import com.mrikso.anitube.app.model.ScreenshotModel;
import com.mrikso.anitube.app.utils.ParserUtils;

public class ScreenshotsAdapter extends ListAdapter<ScreenshotModel, ScreenshotsAdapter.ViewHolder> {

    private static final String TAG = "ScreenshotsAdapter";
    private OnItemClickListener listener;

    public ScreenshotsAdapter() {
        super(new ScreenshotsDiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemScreenshotBinding binding = ItemScreenshotBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScreenshotModel model = getItem(position);
        holder.bind(model, position);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemScreenshotBinding binding;

        public ViewHolder(@NonNull ItemScreenshotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ScreenshotModel model, int position) {
            binding.progressIndicator.setVisibility(View.VISIBLE);

            String imageUrl = ParserUtils.normaliseImageUrl(model.getPreviewUrl());

            DrawableCrossFadeFactory factory =
                    new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

            Glide.with(binding.getRoot().getContext())
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(factory))
                    .error(R.drawable.ic_broken_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(
                                @Nullable GlideException e,
                                Object model,
                                @NonNull Target<Drawable> target,
                                boolean isFirstResource) {
                            binding.progressIndicator.setVisibility(View.GONE);
                            binding.sivScreenshot.setImageDrawable(AppCompatResources.getDrawable(binding.getRoot().getContext(), R.drawable.ic_broken_image));
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
                    .into(binding.sivScreenshot);

            // ViewUtils.loadImage(binding.sivScreenshot, model.getPreviewUrl());
            if (listener != null) {
                binding.getRoot().setOnClickListener(v -> listener.onScreenshotItemSelected(position));
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onScreenshotItemSelected(int position);
    }
}
