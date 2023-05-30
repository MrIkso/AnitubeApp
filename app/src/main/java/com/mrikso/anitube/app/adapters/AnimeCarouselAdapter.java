package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.databinding.ItemInteresingAnimeBinding;
import com.mrikso.anitube.app.model.InteresingModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class AnimeCarouselAdapter extends RecyclerView.Adapter<AnimeCarouselAdapter.ViewHolder> {
    private List<InteresingModel> results = new ArrayList<>();

    private OnItemClickListener listener;

    public void setResults(List<InteresingModel> results) {
        this.results.addAll(results);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemInteresingAnimeBinding binding = ItemInteresingAnimeBinding.inflate(inflater, parent, false);
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
            ViewUtils.loadImage(binding.carouselImageView, ParserUtils.normaliseImageUrl(model.getPosterUrl()));
            ViewUtils.loadImage(binding.carouselBg, ApiClient.ANIME_CAROUSEL_BG_URL);
            if (listener != null) {
                binding.getRoot().setOnClickListener(v -> listener.onCarouselItemSelected(model.getAnimeUrl()));
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
