package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.mrikso.anitube.app.comparator.ActionDiffCallback;
import com.mrikso.anitube.app.databinding.ItemHomeActionBinding;
import com.mrikso.anitube.app.model.ActionModel;

public class ActionListAdapter extends ListAdapter<ActionModel, ActionListAdapter.ViewHolder> {

    private final RequestManager glide;
    private OnItemClickListener listener;

    public ActionListAdapter(RequestManager glide) {
        super(new ActionDiffCallback());
        this.glide = glide;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemHomeActionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActionModel model = getItem(position);
        // Check for null
        if (model != null) {
            holder.bind(model);
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemHomeActionBinding binding;

        public ViewHolder(@NonNull ItemHomeActionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ActionModel model) {
            binding.actionName.setText(model.getName());
            glide.load(model.getBgUrl()).into(binding.actionBg);

            if (listener != null) {
                binding.getRoot().setOnClickListener(v -> listener.onItemSelected(model.getType()));
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemSelected(int type);
    }
}
