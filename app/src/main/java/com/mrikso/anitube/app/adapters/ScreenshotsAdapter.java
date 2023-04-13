package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mrikso.anitube.app.databinding.ItemScreenshotBinding;
import com.mrikso.anitube.app.model.ScreenshotModel;

import java.util.ArrayList;
import java.util.List;

public class ScreenshotsAdapter extends RecyclerView.Adapter<ScreenshotsAdapter.ViewHolder> {
    private List<ScreenshotModel> results = new ArrayList<>();

    private OnItemClickListener listener;

    public void setResults(List<ScreenshotModel> results) {
        this.results.addAll(results);
        notifyDataSetChanged();
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
        ScreenshotModel model = results.get(position);
        holder.bind(model);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemScreenshotBinding binding;

        public ViewHolder(@NonNull ItemScreenshotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ScreenshotModel model) {

            Glide.with(binding.getRoot().getContext())
                    .load(model.getPreviewUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.sivScreenshot);
            if (listener != null) {
                binding.getRoot()
                        .setOnClickListener(
                                v -> listener.onScreenshotItemSelected(model.getFullUrl()));
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onScreenshotItemSelected(String link);
    }
}
