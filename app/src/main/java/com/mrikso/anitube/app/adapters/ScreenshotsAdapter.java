package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.databinding.ItemScreenshotBinding;
import com.mrikso.anitube.app.model.ScreenshotModel;
import com.mrikso.anitube.app.utils.ViewUtils;

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
        holder.bind(model, position);
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

        public void bind(ScreenshotModel model, int position) {
            ViewUtils.loadImage(binding.sivScreenshot, model.getPreviewUrl());
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
