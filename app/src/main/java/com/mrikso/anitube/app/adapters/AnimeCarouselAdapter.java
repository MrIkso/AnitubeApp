package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mrikso.anitube.app.databinding.ItemInteresingAnimeBinding;
import com.mrikso.anitube.app.model.InteresingModel;
import com.mrikso.anitube.app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

public class AnimeCarouselAdapter extends RecyclerView.Adapter<AnimeCarouselAdapter.ViewHolder> {
    private List<InteresingModel> results = new ArrayList<>();

    private OnItemClickListener listener;
    private ItemInteresingAnimeBinding binding;

    public void setResults(List<InteresingModel> results) {
        this.results.addAll(results);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = ItemInteresingAnimeBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InteresingModel episode = results.get(position);
        holder.bind(episode);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemInteresingAnimeBinding binding;

        public ViewHolder(@NonNull ItemInteresingAnimeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(InteresingModel model) {

            Glide.with(binding.getRoot().getContext())
                    .load(ApiClient.BASE_URL + model.getPosterUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.carouselImageView);
            if (listener != null) {
                binding.getRoot()
                        .setOnClickListener(
                                v -> listener.onCarouselItemSelected(model.getAnimeUrl()));
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onCarouselItemSelected(String link);
    }
}
