package org.ken.sheng.gaugesteplist;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import com.intellij.psi.impl.java.stubs.index.*;

public class StepScanner {

    public static Map<String, List<PsiMethod>> getSteps(@NotNull Project project, Module @NotNull [] modules) {
        return Arrays.stream(modules).collect(Collectors.toMap(Module::getName, module -> getSteps(project, module)));
    }

    public static List<PsiMethod> getSteps(@NotNull Project project, @NotNull Module module) {
        return JavaAnnotationIndex.getInstance().get(
                "Step",
                project,
                module.getModuleScope()
        ).stream().map(anno -> {
            PsiModifierList psiModifierList = (PsiModifierList) anno.getParent();
            return psiModifierList.getParent();
        }).filter(anno -> anno instanceof PsiMethod).map(anno -> (PsiMethod) anno).collect(Collectors.toList());
    }
}
