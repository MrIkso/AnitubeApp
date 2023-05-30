package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.mrikso.anitube.app.comparator.CommentsDiffCallback;
import com.mrikso.anitube.app.databinding.ItemCommentBinding;
import com.mrikso.anitube.app.model.CommentModel;
import com.mrikso.anitube.app.utils.HtmlTextSpanner;
import com.mrikso.anitube.app.utils.ParserUtils;

public class CommentsPagingAdapter extends PagingDataAdapter<CommentModel, CommentsPagingAdapter.ViewHolder> {
    public static final int LOADING_ITEM = 0;
    public static final int COMMENT_ITEM = 1;
    private final RequestManager glide;
    private OnItemClickListener listener;

    public CommentsPagingAdapter(RequestManager glide) {
        super(new CommentsDiffCallback());
        this.glide = glide;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentModel model = getItem(position);
        // Check for null
        if (model != null) {
            holder.bind(model);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // set ViewType
        return position == getItemCount() ? COMMENT_ITEM : LOADING_ITEM;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ItemCommentBinding binding;

        public ViewHolder(@NonNull ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CommentModel comment) {
            binding.tvUsername.setText(comment.getUsername());
            binding.tvUsergroup.setText(comment.getUserGroup());
            binding.tvTime.setText(comment.getTime());
            binding.tvComment.setText(HtmlTextSpanner.spanText(comment.getContent()));

            glide.load(ParserUtils.normaliseImageUrl(comment.getUserAvarar())).into(binding.ivProfile);

            if (listener != null) {
                binding.ivProfile.setOnClickListener(v -> listener.onProfileClicked(comment.getUserLink()));
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onProfileClicked(String link);
    }
}
