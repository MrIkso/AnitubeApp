package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.comparator.CollectionsDiffCallback;
import com.mrikso.anitube.app.databinding.ItemCollectionBinding;
import com.mrikso.anitube.app.model.CollectionModel;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.ViewUtils;

public class CollectionsAdapter extends ListAdapter<CollectionModel, CollectionsAdapter.ViewHolder> {
    private OnItemClickListener listener;

    public CollectionsAdapter() {
        super(new CollectionsDiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(ItemCollectionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionModel currentRelease = getItem(position);
        // Check for null
        if (currentRelease != null) {

            holder.bind(currentRelease);
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemCollectionBinding binding;

        public ViewHolder(@NonNull ItemCollectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CollectionModel collection) {
            binding.title.setText(collection.getNameCollection());
            binding.countAnime.setText(binding.getRoot()
                    .getContext()
                    .getString(R.string.collection_anine_count, collection.getCountAnime()));
            ViewUtils.loadImage(binding.image, ParserUtils.normaliseImageUrl(collection.getPosterUrl()));

            if (listener != null) {
                binding.container.setOnClickListener(v -> listener.onReleaseItemSelected(collection));
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onReleaseItemSelected(CollectionModel collection);
    }
}
