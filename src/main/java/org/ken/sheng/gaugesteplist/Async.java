package org.ken.sheng.gaugesteplist;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.CancellablePromise;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Async {

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(
            1,
            10,
            5L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()
    );

    @NotNull
    public static <R> CancellablePromise<R> runRead(@NotNull Project project, @NotNull Callable<R> background, @NotNull Consumer<R> consumer) {
        return runRead(project, background, ModalityState.defaultModalityState(), consumer);
    }

    @NotNull
    public static <R> CancellablePromise<R> runRead(@NotNull Project project, @NotNull Callable<R> background, @NotNull ModalityState state, @NotNull Consumer<R> consumer) {
        return ReadAction.nonBlocking(background)
                .inSmartMode(project)
                .finishOnUiThread(state, consumer)
                .submit(EXECUTOR);
    }

    @PreDestroy
    public void dispose() {
        EXECUTOR.shutdown();
    }
}
