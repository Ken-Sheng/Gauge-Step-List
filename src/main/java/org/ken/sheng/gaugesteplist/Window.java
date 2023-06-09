package org.ken.sheng.gaugesteplist;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.JBSplitter;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Window extends SimpleToolWindowPanel implements Disposable {

    private final transient Project project;

    private final ExecutorService taskExecutor;

    private final GaugeStepListPanel panel;

    public Window(Project project) {
        super(false, false);

        this.project = project;
        this.taskExecutor = new ThreadPoolExecutor(
            1,
            1,
            5L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy()
        );
        this.panel = new GaugeStepListPanel(project);

        // init toolbar

        JBSplitter content = initContent();
        setContent(content);

        // init event

        DumbService.getInstance(project).smartInvokeLater(
            () -> Async.runRead(project, () -> StepScanner.getSteps(project,  ModuleManager.getInstance(project).getModules()), this::renderStepTree)
        );

        Disposer.register(this.project, this);
    }

    private void renderStepTree(Map<String, List<PsiMethod>> method) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Reload gauge steps", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);

                Map<PsiMethod, StepNode<String>> serviceNodes = new HashMap<>();
                Callable<RootNode> producer = () -> {
                    if (indicator.isCanceled()) {
                        return null;
                    }
                    RootNode root = new RootNode("Find empty");
                    indicator.setText("Initialize");

                    method.entrySet().stream()
                            .map(entry -> {
                                String itemName = entry.getKey();
                                List<PsiMethod> steps = entry.getValue();
                                if (steps == null || steps.isEmpty()) {
                                    return null;
                                }
                                ModuleNode<String> moduleNode = new ModuleNode<>(itemName);
                                Map<PsiClass, List<GaugeStep>> listMap = steps.stream().collect(
                                        Collectors.toMap(
                                                // key: PsiClass
                                                gaugeStep -> {
                                                    NavigatablePsiElement psiElement = gaugeStep.getPsiElement();
                                                    PsiElement parent = psiElement.getParent();
                                                    if (parent instanceof PsiClass) {
                                                        return ((PsiClass) parent);
                                                    }
                                                    return null;
                                                },
                                                // value: List<ApiService>
                                                gaugeStep -> new ArrayList<>(Collections.singletonList(gaugeStep)),
                                                // key冲突时的操作
                                                (list1, list2) -> {
                                                    list1.addAll(list2);
                                                    return list1;
                                                }
                                        )
                                );
                                List<StepNode<?>> children = ModuleNode.Util.getChildren(serviceNodes, listMap, showClass);
                                children.forEach(moduleNode::add);
                                return moduleNode;
                            })
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparing(ModuleNode::getFragment))
                            .forEach(root::add);
                    indicator.setText("Waiting to re-render");
                    return root;
                };
                Consumer<RootNode> consumer = root -> {
                    if (root == null) {
                        return;
                    }

                    panel.renderAll(root, serviceNodes);
                };
                ReadAction.nonBlocking(producer)
                        .finishOnUiThread(ModalityState.defaultModalityState(), consumer)
                        .submit(taskExecutor);
            }
        });
    }

    private JBSplitter initContent() {
        JBSplitter contentView = new JBSplitter(true, Window.class.getName(), 0.5F);
        contentView.setFirstComponent(this.panel);
        return contentView;
    }

    @Override
    public void dispose() {
        taskExecutor.shutdown();
    }
}
