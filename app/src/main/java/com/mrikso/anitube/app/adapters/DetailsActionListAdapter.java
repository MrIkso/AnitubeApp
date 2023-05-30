package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.comparator.MenuActionDiffCallback;
import com.mrikso.anitube.app.databinding.ItemMenuActionBinding;
import com.mrikso.anitube.app.model.MenuActionModel;

public class DetailsActionListAdapter extends ListAdapter<MenuActionModel, DetailsActionListAdapter.ViewHolder> {

    private OnItemClickListener listener;

    public DetailsActionListAdapter(OnItemClickListener listener) {
        super(new MenuActionDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemMenuActionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuActionModel model = getItem(position);
        // Check for null
        if (model != null) {
            holder.bind(model);
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemMenuActionBinding binding;

        public ViewHolder(@NonNull ItemMenuActionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MenuActionModel model) {
            binding.menuActionChip.setTag(model.getName());
            binding.menuActionChip.setChipIconResource(model.getIcon());

            if (listener != null) {
                binding.getRoot().setOnClickListener(v -> listener.onItemSelected(model.getId()));
            }
        }
    }

    public interface OnItemClickListener {
        void onItemSelected(int id);
    }
}
