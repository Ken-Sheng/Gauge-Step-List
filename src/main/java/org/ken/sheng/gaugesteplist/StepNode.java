package org.ken.sheng.gaugesteplist;


import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public class StepNode<T> extends DefaultMutableTreeNode implements ISource {

    private T source;

    public StepNode(T source) {
        super(source);
    }

    @Override
    public @NotNull String getFragment() {
        return String.valueOf(source);
    }
}
