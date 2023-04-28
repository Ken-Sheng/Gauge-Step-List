package org.ken.sheng.gaugesteplist;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;

public class ToolWindowServiceImpl implements ToolWindowService{

    private final Project project;

    public ToolWindowServiceImpl(Project project) {
        this.project = project;
    }
    @Override
    public JComponent getContent() {
        return new Window(project);
    }
}
