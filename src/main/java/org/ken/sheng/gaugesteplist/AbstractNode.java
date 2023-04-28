package org.ken.sheng.gaugesteplist;

import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;

public class AbstractNode <T> extends DefaultMutableTreeNode {

    @NotNull
    private T source;

    public AbstractNode(@NotNull T source) {
        super(source);
        this.source = source;
    }

    public @NotNull T getSource() {
        return source;
    }

    public void setSource(@NotNull T source) {
        this.source = source;
    }
}
