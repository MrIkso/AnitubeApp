package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.comparator.CollectionsDiffCallback;
import com.mrikso.anitube.app.databinding.ItemCollectionBinding;
import com.mrikso.anitube.app.model.CollectionModel;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.ViewUtils;

public class CollectionsPagingAdapter extends PagingDataAdapter<CollectionModel, CollectionsPagingAdapter.ViewHolder> {
    public static final int LOADING_ITEM = 0;
    public static final int COLLECTION_ITEM = 1;
    private OnItemClickListener listener;

    public CollectionsPagingAdapter() {
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

    @Override
    public int getItemViewType(int position) {
        // set ViewType
        return position == getItemCount() ? COLLECTION_ITEM : LOADING_ITEM;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemCollectionBinding binding;

        public ViewHolder(@NonNull ItemCollectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CollectionModel collection) {
            binding.title.setText(collection.getNameCollection());
            if (!Strings.isNullOrEmpty(collection.getAuthor())) {
                binding.author.setVisibility(View.VISIBLE);
                binding.author.setText(collection.getAuthor());
            }
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
