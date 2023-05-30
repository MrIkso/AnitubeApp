package com.mrikso.treeview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TreeView extends RecyclerView {

    public TreeView(@NonNull Context context) {
        super(context);
        init();
    }

    public TreeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TreeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public <VH extends ViewHolder, T> void setAdapter(@Nullable Adapter<VH, T> adapter) {
        super.setAdapter(adapter);

        if (adapter != null) {
            adapter.setIdentationWidth(
                    getResources().getDimensionPixelSize(R.dimen.treeview_default_indentation_width));
        }
    }

    public abstract static class Adapter<VH extends ViewHolder, T> extends RecyclerView.Adapter<VH> {
        private static final int ANIM_TIME = 200;
        private int indentationWidth;
        public Adapter.OnTreeItemClickListener<T> listener;
        protected List<TreeItem<T>> items = new ArrayList<>();
        private boolean showRoot = false;
        private boolean disablePadding = false;
        private boolean disableExpandIcon = false;

        public abstract VH onCreateViewHolder(View view);

        public abstract int getLayoutRes();

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_treeview, parent, false);
            inflater.inflate(getLayoutRes(), view.findViewById(R.id.content), true);
            return onCreateViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            TreeItem<T> item = items.get(position);
            View itemView = holder.itemView;
            ImageView leadingIcon = holder.leadingIcon;
            if (disablePadding) {
                itemView.setPadding(computeIndentPadding(item), 0, 0, 0);
            }
            if (disableExpandIcon) {
                if (item.isExpandable()) {

                    if (item.isExpanded()) {
                        leadingIcon.setRotation(90);
                    } else {
                        leadingIcon.setRotation(0);
                    }

                    leadingIcon.setVisibility(View.VISIBLE);
                } else {
                    leadingIcon.setVisibility(View.INVISIBLE);
                }
            } else {
                leadingIcon.setVisibility(View.GONE);
            }

            itemView.setOnClickListener((v) -> {
                if (listener != null) {
                    listener.onClick(item);
                }

                // click with debounce
                long lastClickTime = (long) itemView.getTag();
                if (System.currentTimeMillis() - lastClickTime < ANIM_TIME) {
                    return;
                }
                itemView.setTag(System.currentTimeMillis());

                if (item.isExpandable()) {
                    if (item.isExpanded()) {
                        int removedCount = removeAllExpandedItemRecursive(item, holder.getLayoutPosition() + 1);
                        notifyItemRangeRemoved(holder.getLayoutPosition() + 1, removedCount);
                    } else {
                        item.setExpanded(true);
                        int insertedCount = insertAllExpandedItemRecursive(item, holder.getLayoutPosition() + 1);
                        notifyItemRangeInserted(holder.getLayoutPosition() + 1, insertedCount);
                        item.setExpanded(false);
                    }

                    leadingIcon
                            .animate()
                            .setDuration(ANIM_TIME)
                            .rotationBy(item.isExpanded() ? -90 : 90)
                            .start();

                    item.setExpanded(!item.isExpanded());
                }
            });
        }

        private int computeIndentPadding(TreeItem<T> item) {
            int depth = item.getDepth();
            if (!showRoot) depth--;
            return indentationWidth * depth;
        }

        private int removeAllExpandedItemRecursive(TreeItem<T> root, int index) {
            if (root == null || !root.isExpanded()) return 0;

            int cnt = 0;
            for (TreeItem<T> child : root.getChildren()) {
                items.remove(index);
                cnt += removeAllExpandedItemRecursive(child, index) + 1;
            }

            return cnt;
        }

        private int insertAllExpandedItemRecursive(TreeItem<T> root, int index) {
            if (root == null || !root.isExpanded()) return 0;

            int cnt = 0;
            for (TreeItem<T> child : root.getChildren()) {
                items.add(index + cnt, child);
                cnt += insertAllExpandedItemRecursive(child, index + cnt + 1) + 1;
            }

            return cnt;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void setRoot(TreeItem<T> root) {
            setRoot(root, false);
        }

        public void setRoot(TreeItem<T> root, boolean showRoot) {
            items.clear();

            if (showRoot) {
                items.add(root);

                if (root.isExpanded()) {
                    items.addAll(root.getChildren());
                }
            } else {
                items.addAll(root.getChildren());
            }

            notifyDataSetChanged();

            this.showRoot = showRoot;
        }

        public void setTreeItemClickListener(Adapter.OnTreeItemClickListener<T> listener) {
            this.listener = listener;
        }

        void setIdentationWidth(int width) {
            this.indentationWidth = width;
        }

        public interface OnTreeItemClickListener<T> {
            void onClick(TreeItem<T> item);
        }

        public void setDisablePadding(boolean disablePadding) {
            this.disablePadding = disablePadding;
        }

        public void setDisableExpandIcon(boolean disableExpandIcon) {
            this.disableExpandIcon = disableExpandIcon;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView leadingIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            leadingIcon = itemView.findViewById(R.id.leading_icon);
            itemView.setTag(System.currentTimeMillis());
        }
    }
}
