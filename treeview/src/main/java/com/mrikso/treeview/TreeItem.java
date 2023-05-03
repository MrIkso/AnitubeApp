package com.mrikso.treeview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

public class TreeItem<T> implements Serializable {
    private final T value;
    private final List<TreeItem<T>> children;
    private boolean expanded;
    private boolean expandable;
    private boolean selected;
    protected TreeItem<T> parent;

    public TreeItem() {
        this(null);
    }

    public TreeItem(@NonNull T value) {
        this.value = value;
        this.children = new TreeItemChildrenList<T>(this);
    }

    public boolean isExpanded() {
        return expanded;
    }

    /** @throws IllegalStateException if item is not expandable */
    public void setExpanded(boolean expanded) {
        ensureSelfIsExpandable();
        this.expanded = expanded;
    }

    public boolean isExpandable() {
        expandable = !getChildren().isEmpty();
        return expandable;
    }

    public T getValue() {
        return value;
    }

    @Nullable
    public TreeItem<T> getParent() {
        return parent;
    }

    @NonNull
    public List<TreeItem<T>> getChildren() {
        return children;
    }

    public void addChild(TreeItem<T> child) {
        this.children.add(child);
    }

    public int getDepth() {
        if (parent == null) return 0;
        return parent.getDepth() + 1;
    }
    /*
    @Override
    public String toString() {
      return "TreeItem(" +
          "value=" + value +
          ", expanded=" + expanded +
          ')';
    }
    */

    @Override
    public String toString() {
        return printTree(0);
    }

    private static final int indent = 2;

    private String printTree(int increment) {
        String s = "";
        String inc = "";
        for (int i = 0; i < increment; ++i) {
            inc = inc + " ";
        }
        s = inc + value;
        for (TreeItem<T> child : children) {
            s += "\n" + "depth: " + child.getDepth() + child.printTree(increment + indent);
        }
        return s;
    }

    private void ensureSelfIsExpandable() {
        if (!expandable) {
            // throw new IllegalStateException(this.toString() + " is not expandable.");
        }
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
