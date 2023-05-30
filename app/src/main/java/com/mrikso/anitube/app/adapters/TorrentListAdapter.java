package com.mrikso.anitube.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.comparator.TorrentModelDiffCallback;
import com.mrikso.anitube.app.databinding.ItemTorrentBinding;
import com.mrikso.anitube.app.interfaces.OnTorrentClickListener;
import com.mrikso.anitube.app.model.TorrentModel;

public class TorrentListAdapter extends ListAdapter<TorrentModel, TorrentListAdapter.ViewHolder> {

    private final OnTorrentClickListener listener;

    public TorrentListAdapter(OnTorrentClickListener listener) {
        super(new TorrentModelDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTorrentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TorrentModel model = getItem(position);
        // Check for null
        if (model != null) {
            holder.bind(model);
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemTorrentBinding binding;

        public ViewHolder(@NonNull ItemTorrentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TorrentModel model) {
            Context ctx = binding.getRoot().getContext();
            binding.title.setText(model.getName());
            binding.size.setText(model.getSize());
            binding.downloadCount.setText(String.valueOf(model.getDownloadedCount()));
            binding.leechCount.setText(String.valueOf(model.getLeechers()));
            binding.seedCount.setText(String.valueOf(model.getSeeds()));

            if (listener != null) {
                binding.magnetLink.setOnClickListener(v -> listener.onDownloadByMagnet(model.getMagnetUrl()));
                binding.torrentLink.setOnClickListener(v -> listener.onDownloadTorrent(model.getTorrentUrl()));
            }
        }
    }
}
