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
import com.mrikso.anitube.app.utils.ViewUtils;
import com.mrikso.treeview.TreeItem;

import java.util.HashMap;
import java.util.Map;

public class TreeViewGroup extends LinearLayout {
    //  protected List<TreeItem<PlayerModel>> items = new ArrayList<>();
    /// protected List<Integer> selectedIndex = new ArrayList<>();
    protected TreeItem<PlayerModel> model;
    protected Map<String, Pair<Integer, Integer>> chipGroups = new HashMap<>();
    private boolean showRoot = false;
    private LayoutInflater inflater;
    private OnTreeItemClickListener<PlayerModel> clickListener;
    private OnTreeRestoreListener restoreListener;
    private static final int ANIM_TIME = 200;
    int viewIndex = 0;

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
    }

    public void setRoot(TreeItem<PlayerModel> root) {
        setRoot(root, false);
    }

    public void setRoot(TreeItem<PlayerModel> root, boolean showRoot) {
        if (root == null) return;
        this.model = root;
        chipGroups.clear();

        Log.i("TreeViewGroup", "Set root called");
        // showAllFistNode(root);
        showLevel(root, "root", 0, viewIndex);
        viewIndex++;

        this.showRoot = showRoot;
    }

    private void showLevel(TreeItem<PlayerModel> root, String key, int depth, int viewIndex) {
        if (root == null) return;
        Log.i(
                "TreeViewGroup",
                "showLevel key: " + key + " depth: " + depth + " viewIndex: " + viewIndex);
        if (chipGroups.containsKey(key)) {

            //	showAllChildrenRecursive(root);
            findViewById(chipGroups.get(key).second).setVisibility(VISIBLE);

        } else {
            ChipGroup chipGroup = createChipGroup(root);
            Log.i("TreeViewGroup", "create new node");
            chipGroups.put(key, new Pair<>(depth, chipGroup.getId()));
            addView(chipGroup, viewIndex);
        }
    }

    int showIndex = 0;

    private void showAllChildrenRecursive(TreeItem<PlayerModel> root) {
        if (root == null) return;

        for (TreeItem<PlayerModel> child : root.getChildren()) {
            if (child.isSelected()) {
                Log.i("TreeViewGroup", "restoreState child: " + child);
                if (restoreListener != null) {
                    Log.i("TreeViewGroup", "restoreState restoreListener to fragment");
                    restoreListener.onRestored(child);
                }
                showLevel(child, child.getValue().getId(), child.getDepth(), showIndex);
                showIndex++;
            }
            showAllChildrenRecursive(child);
        }
    }

    private void showAllFistNode(TreeItem<PlayerModel> root) {
        if (root == null) return;
        TreeItem<PlayerModel> children = root.getChildren().stream().findFirst().orElse(null);
        if (children != null) {
            children.setSelected(true);
            children.setExpanded(true);
            String id;
            if (children.getValue() != null) {
                id = "root";
            }
            id = children.getValue().getId();

            showLevel(children, id, children.getDepth(), viewIndex);
            viewIndex++;

            showAllFistNode(children);
        }
    }

    private void removeLevel(String key, int depth) {
        Log.i(
                "TreeViewGroup",
                "removeLevel key: " + key + " depth: " + depth + " viewIndex: " + viewIndex);
        for (Map.Entry<String, Pair<Integer, Integer>> entry : chipGroups.entrySet()) {
            String entryKey = entry.getKey();
            Pair<Integer, Integer> value = entry.getValue();
            int entryDepth = value.first;
            int entryId = value.second;
            if (entryKey.equals("root")) {
                return;
            }
            if (!key.contains(entryKey) && entryDepth >= depth) {
                View nextChild = findViewById(entryId);
                Log.i("TreeViewGroup", "exits node: " + value);
                nextChild.setVisibility(GONE);
            }
        }
    }

    private void removeAllExpandedItemRecursive(TreeItem<PlayerModel> root) {
        if (root == null) return;
        for (TreeItem<PlayerModel> child : root.getChildren()) {

            removeLevel(child.getValue().getId(), child.getDepth());
            removeAllExpandedItemRecursive(child);
        }
    }

    /*
        private int removeAllExpandedItemRecursive(TreeItem<PlayerModel> root, int index) {
            if (root == null || !root.isExpanded()) return 0;

            int cnt = 0;
            for (TreeItem<PlayerModel> child : root.getChildren()) {
                items.remove(index);
                cnt += removeAllExpandedItemRecursive(child, index) + 1;
            }

            return cnt;
        }

        private int insertAllExpandedItemRecursive(TreeItem<PlayerModel> root, int index) {
            if (root == null || !root.isExpanded()) return 0;

            int cnt = 0;
            for (TreeItem<PlayerModel> child : root.getChildren()) {
                int i = index + cnt;
                items.add(i, child);

                cnt += insertAllExpandedItemRecursive(child, index + cnt + 1) + 1;
            }

            return cnt;
        }
    */
    public void setTreeItemClickListener(OnTreeItemClickListener<PlayerModel> listener) {
        this.clickListener = listener;
    }

    public void setTreeRestoreListener(OnTreeRestoreListener listener) {
        this.restoreListener = listener;
    }

    private ChipGroup createChipGroup(TreeItem<PlayerModel> result) {

        ChipGroup chipGroup = ItemChipGroupBinding.inflate(inflater, this, false).getRoot();
        chipGroup.removeAllViews();
        chipGroup.setSingleSelection(true);
        chipGroup.setSelectionRequired(true);
        chipGroup.setId(View.generateViewId());
        int bgColor = ViewUtils.getRandomMaterialColor(getContext());
        for (TreeItem<PlayerModel> model : result.getChildren()) {
            Chip chip =
                    createChip(
                            model.getValue().getName(),
                            bgColor,
                            v -> {
                                if (clickListener != null) {
                                    clickListener.onClick(model);
                                }
                                // click with debounce
                                long lastClickTime = (long) v.getTag();
                                if (System.currentTimeMillis() - lastClickTime < ANIM_TIME) {
                                    return;
                                }
                                v.setTag(System.currentTimeMillis());
                                model.setSelected(!model.isSelected());
                                //  removeAllExpandedItemRecursive(model);
                                // showLevel(model, model.getDepth());
                                // v.setChecked(model.isSelected());

                                if (model.isExpandable()) {
                                    // if (model.isExpanded()) {
                                    // model.setExpanded(false);
                                    //     removeAllExpandedItemRecursive(result);
                                    //  } else {
                                    // model.setExpanded(true);
                                    // removeLevel(model.getValue().getId(), model.getDepth());
                                    removeAllExpandedItemRecursive(result);

                                    showLevel(
                                            model,
                                            model.getValue().getId(),
                                            model.getDepth(),
                                            viewIndex);

                                    if (!model.isExpanded()) {
                                        viewIndex++;
                                    }
                                    model.setExpanded(true);
                                    //   }
                                    //  model.setExpanded(!model.isExpanded());
                                    // v.setSelected(model.isExpanded());
                                    // } else {
                                    // model.setExpanded(true);
                                    // showLevel(model, model.getDepth());
                                    // v.setSelected(!v.isSelected());
                                }

                                // removeLevel(model.getDepth());
                                // showLevel(model, model.getDepth());
                                //   v.setSelected(!model.isExpanded());
                            });

            chip.setChecked(model.isSelected());
            chipGroup.addView(chip);
        }

        return chipGroup;
    }

    private Chip createChip(String name, @ColorInt int color, View.OnClickListener listener) {
        Chip chip = new Chip(getContext(), null, R.style.Widget_Material3_Chip_Filter_Elevated);
        chip.setText(name);
        chip.setTextColor(
                ContextCompat.getColorStateList(
                        getContext(), com.mrikso.anitube.app.R.color.text_200));
        chip.setClickable(true);
        chip.setCheckable(true);
        chip.setOnClickListener(listener);
        chip.setChipBackgroundColor(ColorStateList.valueOf(color));
        chip.setId(View.generateViewId());
        chip.setTag(System.currentTimeMillis());
        return chip;
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
        int childCount = getChildCount();
        SavedState ss = new SavedState(superState);
        ss.model = model;
        //  ss.children = selectedIndex;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        // this.removeAllViews();
        model = ss.model;
        Log.i("TreeViewGroup", "onRestoreInstanceState: " + model.toString());
        showAllChildrenRecursive(model);
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
