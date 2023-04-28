package org.ken.sheng.gaugesteplist;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class GaugeStepScan extends AnAction {

    private Project project;

    @Override
    public void actionPerformed(AnActionEvent e) {
//        Project project = e.getData(PlatformDataKeys.PROJECT);
//        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
//        String classPath = psiFile.getVirtualFile().getPath();
//        String title = "Hello World!";
//        Messages.showMessageDialog(project, classPath, title, Messages.getInformationIcon());

        Project project = e.getData(PlatformDataKeys.PROJECT);
        this.project = project;
        File rootFile = new File(project.getBasePath());
        Set<Class<?>> allClasses = new HashSet<>();
        try {
            this.traversal(rootFile, new HashSet<>());
        } catch (ClassNotFoundException ex) {
            // handle exception
        }
        System.out.println(allClasses);
    }

    private void traversal(File file, Set<Class<?>> allClasses) throws ClassNotFoundException {
        if (file == null) {
            return;
        }
        if (!file.isDirectory()) {
            if (file.getPath().endsWith(".java")) {
               String relativePath = file.getPath().replaceAll("\\\\", "/");
               relativePath = relativePath.replace(project.getBasePath(), "");
               relativePath = relativePath.replaceAll("/", ".");
               relativePath = relativePath.replace(".java", ".class");
               allClasses.add(Class.forName(relativePath));
            }
        }
        File[] subFiles = file.listFiles();
        if (subFiles != null) {
            for (int i = 0; i < subFiles.length; i++) {
                this.traversal(subFiles[i], allClasses);
            }
        }
    }
}
