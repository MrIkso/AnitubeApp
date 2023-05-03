package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mrikso.anitube.app.databinding.ItemBestAnimeBinding;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

public class BaseAnimeAdapter extends RecyclerView.Adapter<BaseAnimeAdapter.ViewHolder> {
    private List<BaseAnimeModel> results = new ArrayList<>();

    private OnItemClickListener listener;
    private ItemBestAnimeBinding binding;

    public void setResults(List<BaseAnimeModel> results) {
        this.results.addAll(results);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = ItemBestAnimeBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BaseAnimeModel episode = results.get(position);
        holder.bind(episode);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemBestAnimeBinding binding;

        public ViewHolder(@NonNull ItemBestAnimeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(BaseAnimeModel episode) {
            binding.tvTitle.setText(episode.title);
            Glide.with(binding.getRoot().getContext())
                    .load(ApiClient.BASE_URL + episode.posterUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.sivPoster);
            if (listener != null) {
                binding.getRoot()
                        .setOnClickListener(v -> listener.onBaseItemSelected(episode.animeUrl));
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onBaseItemSelected(String link);
    }
}
