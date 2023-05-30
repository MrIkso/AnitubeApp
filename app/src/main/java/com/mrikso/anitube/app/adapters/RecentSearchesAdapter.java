package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.data.search.RecentSearch;
import com.mrikso.anitube.app.databinding.ItemSearchBinding;
import com.mrikso.anitube.app.databinding.ItemSearchHeaderBinding;

import java.util.ArrayList;
import java.util.List;

public class RecentSearchesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<RecentSearch> searchesList = new ArrayList<>();
    private OnItemClickListener listener;

    public void setData(List<RecentSearch> searchesList) {
        this.searchesList = searchesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ITEM) {
            ItemSearchBinding binding = ItemSearchBinding.inflate(inflater, parent, false);
            return new ItemViewHolder(binding);
        } else if (viewType == TYPE_HEADER) {
            ItemSearchHeaderBinding header = ItemSearchHeaderBinding.inflate(inflater, parent, false);
            return new HeaderViewHolder(header);
        } else return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder itemHolder = (HeaderViewHolder) holder;
            itemHolder.bind();
        } else if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            RecentSearch recentSearch = searchesList.get(position);
            itemHolder.bind(recentSearch);
        }
    }

    @Override
    public int getItemCount() {
        return searchesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    protected class ItemViewHolder extends RecyclerView.ViewHolder {
        private ItemSearchBinding binding;

        public ItemViewHolder(@NonNull ItemSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RecentSearch model) {
            binding.queryTv.setText(model.getSearch_name());
            if (listener != null) {
                binding.getRoot().setOnClickListener(v -> listener.onRecentItemClicked(model.getSearch_name()));
            }
        }
    }

    protected class HeaderViewHolder extends RecyclerView.ViewHolder {
        private ItemSearchHeaderBinding binding;

        public HeaderViewHolder(@NonNull ItemSearchHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind() {
            if (listener != null) {
                binding.clearHistory.setOnClickListener(v -> {
                    listener.onDeleteAllSearchHistory();
                    searchesList.clear();
                    notifyDataSetChanged();
                });
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onRecentItemClicked(String query);

        void onDeleteRecentItemClicked(String query);

        void onDeleteAllSearchHistory();
    }
}
