package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.comparator.FranchiseDiffCallback;
import com.mrikso.anitube.app.databinding.ItemFranchiseBinding;
import com.mrikso.anitube.app.model.FranchiseModel;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.ViewUtils;

public class FranchisesAdapter extends ListAdapter<FranchiseModel, FranchisesAdapter.ViewHolder> {
    private final OnItemClickListener listener;

    public FranchisesAdapter(OnItemClickListener listener) {
        super(new FranchiseDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemFranchiseBinding binding = ItemFranchiseBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FranchiseModel release = getItem(position);
        holder.bind(release);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemFranchiseBinding binding;

        public ViewHolder(@NonNull ItemFranchiseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FranchiseModel release) {
            binding.tvTitle.setText(release.getTitle());
            binding.tvYear.setText(release.getReleaseYear());
            binding.tvEpisodes.setText(release.getEpisodes());
            ViewUtils.loadImage(binding.poster, ParserUtils.normaliseImageUrl(release.getPosterUrl()));
            if (release.isCurrent()) {
                binding.getRoot().setEnabled(false);
                binding.getRoot()
                        .setBackgroundColor(
                                ContextCompat.getColor(binding.getRoot().getContext(), R.color.grey_transpatent));
            } else {
                if (listener != null) {
                    binding.getRoot().setOnClickListener(v -> listener.onReleaseItemSelected(release.getAnimeUrl()));
                }
            }
        }
    }

    public interface OnItemClickListener {
        void onReleaseItemSelected(String url);
    }
}
