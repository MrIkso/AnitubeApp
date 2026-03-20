package com.mrikso.anitube.app.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.google.android.material.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.mrikso.anitube.app.databinding.ItemChipGroupBinding;
import com.mrikso.anitube.app.parser.video.model.PlayerModel;
import com.mrikso.anitube.app.utils.ListUtils;
import com.mrikso.anitube.app.utils.ViewUtils;
import com.mrikso.treeview.TreeItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeViewGroup extends LinearLayout {
    protected TreeItem<PlayerModel> model;
    protected Map<String, Pair<Integer, Integer>> chipGroups = new HashMap<>();
    private LayoutInflater inflater;
    private OnTreeItemClickListener<PlayerModel> clickListener;
    private OnTreeRestoreListener restoreListener;

    private Map<String, Integer> savedLevelColors = new HashMap<>();

    public TreeViewGroup(Context context) {
        super(context);
        init();
    }

    public TreeViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TreeViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TreeViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void init() {
        inflater = LayoutInflater.from(getContext());
        setOrientation(VERTICAL);
        setSaveEnabled(true);
    }

    public void setRoot(TreeItem<PlayerModel> root) {
        if (root == null)
            return;

        if (this.model != null && this.model.getValue() != null && root.getValue() != null) {
            if (this.model.getValue().getId().equals(root.getValue().getId())) {
                Log.i("TreeViewGroup", "Root already set/restored, skipping render");
                return;
            }
        }

        this.model = root;
        this.chipGroups.clear();
        this.savedLevelColors.clear();
        this.removeAllViews();

        renderRestoredTree(root);
    }

    private void showLevel(TreeItem<PlayerModel> parent, String key, int depth, int ignoredIndex) {
        if (parent == null || parent.getChildren().isEmpty())
            return;

        ChipGroup chipGroup = createChipGroup(parent, key);
        chipGroups.put(key, new Pair<>(depth, chipGroup.getId()));
        addView(chipGroup);
    }

    public String getPath(TreeItem<PlayerModel> root) {
        StringBuilder path = new StringBuilder();
        TreeItem<PlayerModel> parent = root.getParent();
        List<String> pathList = new ArrayList<>();
        while (parent != null) {
            if (parent.getValue() != null) {
                pathList.add(parent.getValue().getName());
            }
            parent = parent.getParent();
        }

        pathList = ListUtils.reverseList(pathList);

        for (String ss : pathList) {
            path.append(ss);
            path.append("->");
        }

        path.append(root.getValue().getName());

        return path.toString();
    }

    public void setTreeItemClickListener(OnTreeItemClickListener<PlayerModel> listener) {
        this.clickListener = listener;
    }

    public void setTreeRestoreListener(OnTreeRestoreListener listener) {
        this.restoreListener = listener;
    }

    private ChipGroup createChipGroup(TreeItem<PlayerModel> parentItem, String key) {
        ItemChipGroupBinding binding = ItemChipGroupBinding.inflate(inflater, this, false);
        ChipGroup chipGroup = binding.getRoot();
        chipGroup.removeAllViews();
        chipGroup.setSingleSelection(true);
        chipGroup.setSelectionRequired(true);
        chipGroup.setId(View.generateViewId());

        int bgColor;
        if (savedLevelColors.containsKey(key)) {
            bgColor = savedLevelColors.get(key);
        } else {
            bgColor = ViewUtils.getRandomMaterialColor(getContext());
            savedLevelColors.put(key, bgColor);
        }

        for (TreeItem<PlayerModel> currentItem : parentItem.getChildren()) {
            Chip chip = createChip(currentItem.getValue().getName(), bgColor, v -> {
                for (TreeItem<PlayerModel> sibling : parentItem.getChildren()) {
                    sibling.setSelected(false);
                }

                currentItem.setSelected(true);
                removeGroupsBelowDepth(currentItem.getDepth());
                if (currentItem.isExpandable()) {
                    currentItem.setExpanded(true);
                    autoSelectFirstChildRecursive(currentItem);
                }

                if (clickListener != null) {
                    clickListener.onClick(currentItem);
                }
            });

            chip.setChecked(currentItem.isSelected());
            chipGroup.addView(chip);
        }

        return chipGroup;
    }

    private Chip createChip(String name, @ColorInt int color, View.OnClickListener listener) {
        Chip chip = new Chip(getContext(), null, R.style.Widget_Material3_Chip_Filter_Elevated);
        chip.setText(name);
        chip.setTextColor(ContextCompat.getColorStateList(getContext(), com.mrikso.anitube.app.R.color.text_200));
        chip.setClickable(true);
        chip.setCheckable(true);
        chip.setOnClickListener(listener);
        chip.setChipBackgroundColor(ColorStateList.valueOf(color));
        chip.setId(View.generateViewId());
        chip.setTag(System.currentTimeMillis());
        return chip;
    }

    private void autoSelectFirstChildRecursive(TreeItem<PlayerModel> item) {
        if (item == null || item.getChildren().isEmpty())
            return;

        TreeItem<PlayerModel> firstChild = item.getChildren().get(0);
        firstChild.setSelected(true);

        showLevel(item, item.getValue().getId(), item.getDepth(), -1);

        if (firstChild.isExpandable()) {
            firstChild.setExpanded(true);
            autoSelectFirstChildRecursive(firstChild);
        } else {
            if (clickListener != null) {
                clickListener.onClick(firstChild);
            }
        }
    }


    private void removeGroupsBelowDepth(int depth) {
        List<View> toRemove = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            for (Map.Entry<String, Pair<Integer, Integer>> entry : chipGroups.entrySet()) {
                if (entry.getValue().second == child.getId() && entry.getValue().first >= depth) {
                    toRemove.add(child);
                }
            }
        }

        for (View v : toRemove) {
            removeView(v);
        }

        chipGroups.entrySet().removeIf(entry -> entry.getValue().first >= depth);
    }

    private void renderRestoredTree(TreeItem<PlayerModel> parent) {
        if (parent == null || parent.getChildren().isEmpty()) return;

        String key = (parent.getParent() == null) ? "root" : parent.getValue().getId();

        ChipGroup group = createChipGroup(parent, key);
        chipGroups.put(key, new Pair<>(parent.getDepth(), group.getId()));
        addView(group);

        for (TreeItem<PlayerModel> child : parent.getChildren()) {
            if (child.isSelected()) {
                if (child.isExpandable()) {
                    renderRestoredTree(child);
                } else {
                    if (restoreListener != null)
                        restoreListener.onRestored(child);
                }
                break;
            }
        }
    }

    public interface OnTreeItemClickListener<T> {
        void onClick(TreeItem<PlayerModel> item);
    }

    public interface OnTreeRestoreListener {
        void onRestored(TreeItem<PlayerModel> item);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.model = this.model;
        ss.levelColors = this.savedLevelColors; // Зберігаємо кольори!
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        this.model = ss.model;
        this.savedLevelColors = ss.levelColors != null ? ss.levelColors : new HashMap<>();

        if (this.model != null) {
            this.model.restoreParentLinks(null);

            this.removeAllViews();
            this.chipGroups.clear();

            renderRestoredTree(this.model);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }
}
