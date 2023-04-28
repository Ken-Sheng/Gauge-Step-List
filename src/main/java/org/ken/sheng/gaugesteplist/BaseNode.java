package org.ken.sheng.gaugesteplist;


import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.MutableTreeNode;

public abstract class BaseNode<T> extends AbstractNode<T> implements ISource {

    public BaseNode(T source) {
        super(source);
    }

    public Icon getIcon(boolean selected) {
        return null;
    }

    @Override
    public void add(@NotNull MutableTreeNode newChild) {
        if (!(newChild instanceof BaseNode<?>)) {
            return;
        }
        addNode(((BaseNode<?>) newChild));
    }

    private void addNode(@NotNull BaseNode<?> newChild) {
        super.add(newChild);
    }
}
