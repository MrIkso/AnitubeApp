package com.mrikso.anitube.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.model.ActionItem;
import com.mrikso.anitube.app.utils.ViewUtils;

public class ActionsAdapter extends ListAdapter<ActionItem, ActionsAdapter.ActionViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ActionItem item);
    }

    private final OnItemClickListener listener;

    public ActionsAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull ActionItem oldItem, @NonNull ActionItem newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull ActionItem oldItem, @NonNull ActionItem newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_action, parent, false);
        return new ActionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        ActionItem action = getCurrentList().get(position);
        if (action.getCurrentIconColor() != 0) {
            holder.icon.setImageDrawable(
                    ViewUtils.changeIconColor(
                            holder.itemView.getContext(),
                            action.getCurrentIconResId(),
                            action.getCurrentIconColor()
                    ));
        } else {
            holder.icon.setImageResource(action.getCurrentIconResId());
        }

        if (action.getCurrentDisplayText() != null && !action.getCurrentDisplayText().isEmpty()) {
            holder.text.setText(action.getCurrentDisplayText());
        } else {
            holder.text.setText(action.getDefaultTextResId());
        }

        if (action.isVisible()) {
            holder.itemView.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams.width == 0 && layoutParams.height == 0) {
                layoutParams.width = ConvertUtils.dp2px(75f);
                layoutParams.height = ConvertUtils.dp2px(75f);
                holder.itemView.setLayoutParams(layoutParams);
            }
        } else {
            holder.itemView.setVisibility(View.GONE);
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.width = 0;
            layoutParams.height = 0;
            holder.itemView.setLayoutParams(layoutParams);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(action));
    }

    public static class ActionViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView text;

        public ActionViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.action_icon);
            text = itemView.findViewById(R.id.action_text);
        }
    }

}
