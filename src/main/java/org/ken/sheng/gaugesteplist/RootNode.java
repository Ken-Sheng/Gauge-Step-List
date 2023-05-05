package org.ken.sheng.gaugesteplist;

import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;

public class RootNode extends DefaultMutableTreeNode implements ISource{

    private String source;

    public RootNode(String source) {
        this.source = source;
    }

    @Override
    public @NotNull String getFragment() {
        return source;
    }
}
