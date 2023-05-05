package org.ken.sheng.gaugesteplist;

import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public class ModuleNode<T> extends DefaultMutableTreeNode implements ISource{

    private T source;

    public ModuleNode(T source) {
        this.source = source;
    }

    @Override
    public void add(@NotNull MutableTreeNode newChild) {
        if (!(newChild instanceof StepNode<?>)) {
            return;
        }
        addNode(((StepNode<?>) newChild));
    }

    private void addNode(@NotNull StepNode<?> newChild) {
        super.add(newChild);
    }

    @Override
    public @NotNull String getFragment() {
        return String.valueOf(this.source);
    }
}
