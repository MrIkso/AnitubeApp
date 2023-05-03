package com.mrikso.anitube.app.parser.video;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<T> {
    private T data = null;
    private List<TreeNode<T>> children = new ArrayList<>();
    private TreeNode parent = null;
    private int depth;
    private boolean isExpand;

    public TreeNode(T data) {
        this.data = data;
        this.depth = 0; // 0 is the base level (only the root should be on there)
    }

    public int getDepth() {
        return this.depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void addChild(TreeNode<T> child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(T data) {
        TreeNode<T> newChild = new TreeNode<>(data);
        this.addChild(newChild);
    }

    public boolean toggle() {
        isExpand = !isExpand;
        return isExpand;
    }

    public void collapse() {
        if (isExpand) {
            isExpand = false;
        }
    }

    public void expand() {
        if (!isExpand) {
            isExpand = true;
        }
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void addChildren(List<TreeNode<T>> children) {
        for (TreeNode t : children) {
            t.setParent(this);
        }
        this.children.addAll(children);
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private void setParent(TreeNode parent) {
        this.setDepth(parent.getDepth() + 1);
        this.parent = parent;
    }

    public TreeNode getParent() {
        return parent;
    }

    public boolean isRootNode() {
        return (this.parent == null);
    }

    public boolean isLeafNode() {
        return (this.children.size() == 0);
    }

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
        s = inc + data;
        for (TreeNode<T> child : children) {
            s += "\n" + child.printTree(increment + indent);
        }
        return s;
    }
}
