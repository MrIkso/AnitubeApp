package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.comparator.SimpleModelDiffCallback;
import com.mrikso.anitube.app.databinding.ItemSimpleBinding;
import com.mrikso.anitube.app.model.SimpleModel;

public class SimpleAdapter extends ListAdapter<SimpleModel, SimpleAdapter.ViewHolder> {
    private OnItemClickListener listener;

    public SimpleAdapter() {
        super(new SimpleModelDiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSimpleBinding binding = ItemSimpleBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SimpleModel episode = getItem(position);
        holder.bind(episode);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemSimpleBinding binding;

        public ViewHolder(@NonNull ItemSimpleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SimpleModel simpleModel) {
            binding.tvName.setText(simpleModel.getText());
            if (listener != null) {
                binding.readMore.setOnClickListener(
                        v -> listener.onItemSelected(simpleModel.getText(), simpleModel.getUrl()));
                binding.getRoot()
                        .setOnClickListener(v -> listener.onItemSelected(simpleModel.getText(), simpleModel.getUrl()));
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemSelected(String name, String url);
    }
}
