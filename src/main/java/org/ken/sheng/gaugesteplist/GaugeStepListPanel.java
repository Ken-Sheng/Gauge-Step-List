package org.ken.sheng.gaugesteplist;

import com.intellij.icons.AllIcons;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;

public class GaugeStepListPanel extends JBScrollPane implements TreeExpander {

    private final JTree tree;

    public GaugeStepListPanel(Project project) {
        this.tree = tree;

        this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setBorder(new CustomLineBorder(JBUI.insetsTop(1)));

        this.tree.setCellRenderer(new CustomTreeCellRenderer());
        this.tree.setRootVisible(true);
        this.tree.setShowsRootHandles(false);
        this.setViewportView(tree);

        this.tree.addTreeSelectionListener(e -> {
            if (!this.tree.isEnabled()) {
                return;
            }
            Object component = tree.getLastSelectedPathComponent();
            if (!(component instanceof StepNode<?>)) {
                return;
            }
            StepNode<?> node = (StepNode<?>) component;
            if (getChooseListener() != null) {
                getChooseListener().accept(node);
            }
        });
        this.tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (!tree.isEnabled()) {
                    return;
                }
                StepNode<?> node = getNode(event);
                if (node == null) {
                    return;
                }
                if (SwingUtilities.isLeftMouseButton(event)) {
                    if (event.getClickCount() == 2 && getDoubleClickListener() != null) {
                        getDoubleClickListener().accept(node);
                    }
                } else if (SwingUtilities.isRightMouseButton(event)) {
                    showPopupMenu(event.getX(), event.getY(), getPopupMenu(event, node));
                }
            }

            @Nullable
            private StepNode<?> getNode(@NotNull MouseEvent event) {
                TreePath path = tree.getPathForLocation(event.getX(), event.getY());
                tree.setSelectionPath(path);
                return getChooseNode(path);
            }
        });
    }

    protected final JTree getTree() {
        return this.tree;
    }

    protected final DefaultTreeModel getTreeModel() {
        return (DefaultTreeModel) this.tree.getModel();
    }

    public final void render(@NotNull StepNode<?> rootNode) {
        getTreeModel().setRoot(rootNode);
    }

    @Nullable
    public StepNode<?> getChooseNode(@Nullable TreePath treePath) {
        Object component = null;
        if (treePath != null) {
            component = treePath.getLastPathComponent();
        } else {
            component = tree.getLastSelectedPathComponent();
        }
        if (!(component instanceof StepNode<?>)) {
            return null;
        }
        return (StepNode<?>) component;
    }

    public void treeExpand() {
        expandAll(new TreePath(tree.getModel().getRoot()), true);
    }

    public void treeCollapse() {
        expandAll(new TreePath(tree.getModel().getRoot()), false);
    }

    /**
     * 展开tree视图
     *
     * @param parent treePath
     * @param expand 是否展开
     */
    private void expandAll(@NotNull TreePath parent, boolean expand) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
                javax.swing.tree.TreeNode n = (javax.swing.tree.TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(path, expand);
            }
        }

        // 展开或收起必须自下而上进行
        if (expand) {
            tree.expandPath(parent);
        } else {
            if (node.isRoot()) {
                return;
            }
            tree.collapsePath(parent);
        }
    }

    /**
     * 显示右键菜单
     */
    protected void showPopupMenu(int x, int y, @Nullable JPopupMenu menu) {
        if (menu == null) {
            return;
        }
        TreePath path = tree.getPathForLocation(x, y);
        tree.setSelectionPath(path);
        Rectangle rectangle = tree.getUI().getPathBounds(tree, path);
        if (rectangle != null && rectangle.contains(x, y)) {
            menu.show(tree, x, rectangle.y + rectangle.height);
        }
    }

    @Override
    public boolean canExpand() {
        return tree.getRowCount() > 0;
    }

    @Override
    public boolean canCollapse() {
        return tree.getRowCount() > 0;
    }

    @Override
    public void expandAll() {
        expandAll(new TreePath(tree.getModel().getRoot()), true);
    }

    @Override
    public void collapseAll() {
        expandAll(new TreePath(tree.getModel().getRoot()), false);
    }

    @Nullable
    protected JPopupMenu getPopupMenu(@NotNull MouseEvent event, @NotNull StepNode<?> node) {
        List<JMenuItem> items = new ArrayList<>();
        if (node instanceof ClassNode) {
            // navigation
            JMenuItem navigation = new JBMenuItem(Bundle.getString("action.NavigateToClass.text"), AllIcons.Nodes.Class);
            navigation.addActionListener(actionEvent -> {
                ClassTree classTree = ((ClassNode) node).getSource();
                classTree.getPsiClass().navigate(true);
            });
            items.add(navigation);
        } else if (node instanceof ServiceNode) {
            // navigation
            JMenuItem navigation = new JBMenuItem(Bundle.getString("action.NavigateToMethod.text"), AllIcons.Nodes.Method);
            navigation.addActionListener(actionEvent -> {
                GaugeStep gaugeStep = ((ServiceNode) node).getSource();
                gaugeStep.navigate(true);
            });
            items.add(navigation);

            // Copy full url
            JMenuItem copyFullUrl = new JBMenuItem(Bundle.getString("action.CopyFullPath.text"), AllIcons.Actions.Copy);
            copyFullUrl.addActionListener(actionEvent -> {
                GaugeStep gaugeStep = ((ServiceNode) node).getSource();
                SystemUtil.Clipboard.copy(gaugeStep.getRequestUrl());
                Notify.getInstance(project).info(Bundle.getString("action.CopyFullPath.success"));
            });
            items.add(copyFullUrl);

            // Copy api path
            JMenuItem copyApiPath = new JBMenuItem(Bundle.getString("action.CopyApi.text"), AllIcons.Actions.Copy);
            copyApiPath.addActionListener(actionEvent -> {
                GaugeStep gaugeStep = ((ServiceNode) node).getSource();
                SystemUtil.Clipboard.copy(gaugeStep.getPath());
                Notify.getInstance(project).info(Bundle.getString("action.CopyApi.success"));
            });
            items.add(copyApiPath);
        } else if (node instanceof ModuleNode) {
            ModuleTree moduleTree = ((ModuleNode) node).getSource();
            String moduleName = moduleTree.getModuleName();

            JBMenuItem moduleSetting = new JBMenuItem(Bundle.getString("action.OpenModuleSetting.text"), AllIcons.General.Settings);
            moduleSetting.addActionListener(action -> {
                Module module = ModuleManager.getInstance(project).findModuleByName(moduleName);
                if (module == null) {
                    return;
                }
                // 打开当前项目模块设置
                ProjectSettingsService.getInstance(project).openModuleSettings(module);
            });
            items.add(moduleSetting);

            JBMenuItem moduleConfig = new JBMenuItem(Bundle.getString("action.OpenModuleProperties.text"));
            moduleConfig.addActionListener(action -> {
                showPopupMenu(event.getX(), event.getY(), new ModuleConfigPopup(project, moduleName));
            });
            items.add(moduleConfig);

            JBMenuItem moduleHeaders = new JBMenuItem("Module Headers");
            moduleHeaders.addActionListener(action -> {
                showPopupMenu(event.getX(), event.getY(), new ModuleHeadersPopup(project, moduleName));
            });
            items.add(moduleHeaders);
        }

        if (items.isEmpty()) {
            return null;
        }
        JBPopupMenu menu = new JBPopupMenu();
        items.forEach(menu::add);
        return menu;
    }

    @Nullable
    protected abstract Consumer<StepNode<?>> getChooseListener();

    @Nullable
    protected abstract Consumer<StepNode<?>> getDoubleClickListener();
}
