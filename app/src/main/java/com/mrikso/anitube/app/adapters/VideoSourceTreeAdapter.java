package com.mrikso.anitube.app.adapters;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.chip.Chip;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.parser.video.model.PlayerModel;
import com.mrikso.treeview.TreeView;

public class VideoSourceTreeAdapter extends TreeView.Adapter<VideoSourceTreeAdapter.ViewHolder, PlayerModel> {

    public VideoSourceTreeAdapter() {
        // setDisablePadding(false);
        // setDisableExpandIcon(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.name.setText(items.get(position).getValue().getName());
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_tree;
    }

    static class ViewHolder extends TreeView.ViewHolder {
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
        }
    }
}
