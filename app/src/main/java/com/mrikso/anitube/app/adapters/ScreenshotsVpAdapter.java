package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mrikso.anitube.app.comparator.ScreenshotsDiffCallback;
import com.mrikso.anitube.app.databinding.ItemScreenshotBigBinding;
import com.mrikso.anitube.app.model.ScreenshotModel;

public class ScreenshotsVpAdapter
        extends ListAdapter<ScreenshotModel, ScreenshotsVpAdapter.ViewHolder> {

    public ScreenshotsVpAdapter() {
        super(new ScreenshotsDiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemScreenshotBigBinding binding =
                ItemScreenshotBigBinding.inflate(inflater, parent, false);
        binding.tivScreenshot.setOnTouchListener(
                (view, event) -> {
                    boolean result = true;
                    if (event.getPointerCount() >= 2
                            || view.canScrollHorizontally(1) && view.canScrollHorizontally(-1)) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                            case MotionEvent.ACTION_MOVE:
                                // Disallow RecyclerView to intercept touch events.
                                parent.requestDisallowInterceptTouchEvent(true);
                                // Disable touch on view
                                result = false;
                                break;
                            case MotionEvent.ACTION_UP:
                                // Allow RecyclerView to intercept touch events.
                                parent.requestDisallowInterceptTouchEvent(false);
                                result = true;
                                break;
                            default:
                                result = true;
                                break;
                        }
                    }
                    return result;
                });
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemScreenshotBigBinding binding;

        public ViewHolder(@NonNull ItemScreenshotBigBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ScreenshotModel model) {

            Glide.with(binding.getRoot().getContext())
                    .load(model.getFullUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.tivScreenshot);
        }
    }
}
