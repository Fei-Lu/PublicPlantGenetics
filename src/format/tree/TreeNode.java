/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.tree;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author feilu
 */
public class TreeNode<T> {
    
    private T node = null;
 
    private List<TreeNode<T>> children = new ArrayList<>();

    private TreeNode<T> parent = null;

    public TreeNode (T node) {
        this.node = node;
    }

    public TreeNode<T> addChild(TreeNode<T> child) {
        child.setParent(this);
        this.children.add(child);
        return child;
    }

    public void addChildren(List<TreeNode<T>> children) {
        children.forEach(each -> each.setParent(this));
        this.children.addAll(children);
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public T getNode() {
        return node;
    }

    public void setNode(T node) {
        this.node = node;
    }

    private void setParent(TreeNode<T> parent) {
        this.parent = parent;
    }

    public TreeNode<T> getParent() {
        return parent;
    }
    
    public boolean isRoot() {
        return this.parent == null;
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }
}
