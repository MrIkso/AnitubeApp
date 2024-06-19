package com.mrikso.anitube.app.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.comparator.ScreenshotsDiffCallback;
import com.mrikso.anitube.app.databinding.ItemScreenshotBigBinding;
import com.mrikso.anitube.app.model.ScreenshotModel;
import com.mrikso.anitube.app.utils.ViewUtils;

public class ScreenshotsVpAdapter extends ListAdapter<ScreenshotModel, ScreenshotsVpAdapter.ViewHolder> {

    public ScreenshotsVpAdapter() {
        super(new ScreenshotsDiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemScreenshotBigBinding binding = ItemScreenshotBigBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemScreenshotBigBinding binding;

        public ViewHolder(@NonNull ItemScreenshotBigBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ScreenshotModel model) {
            //Log.i("dd", model.toString());
            Glide.with(binding.getRoot().getContext())
                    .asBitmap()
                    .load(model.getFullUrl())
                    .error(R.drawable.ic_broken_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            binding.progressIndicator.setVisibility(View.GONE);
                            binding.tivScreenshot.setImage(ImageSource.bitmap(resource));
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            binding.progressIndicator.setVisibility(View.GONE);
                            binding.tivScreenshot.setZoomEnabled(false);
                            binding.tivScreenshot.setQuickScaleEnabled(false);
                            binding.tivScreenshot.setImage(ImageSource.bitmap(ViewUtils.drawableToBitmap(errorDrawable)));
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }
    }
}
