package org.ken.sheng.gaugesteplist;

import com.intellij.ui.ColoredTreeCellRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CustomTreeCellRenderer extends ColoredTreeCellRenderer {

    @Override
    public void customizeCellRenderer(
            @NotNull JTree tree,
            Object target,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row, boolean hasFocus) {
        BaseNode<?> node = null;
        if (target instanceof BaseNode) {
            node = (BaseNode<?>) target;
        }
        if (node == null) {
            return;
        }
        setIcon(node.getIcon(selected));
        append(node.getFragment(), node.getTextAttributes());
    }
}
