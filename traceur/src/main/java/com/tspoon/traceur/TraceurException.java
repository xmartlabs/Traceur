package com.tspoon.traceur;

import com.tspoon.traceur.Traceur.AssemblyLogLevel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Consumer;

public class TraceurException extends RuntimeException {

    private final boolean shouldFilterStackTraces;
    @NonNull
    private final AssemblyLogLevel assemblyLogLevel;
    @Nullable
    private final Consumer<Throwable> throwableConsumer;

    /**
     * Creates an instance of a {@code TraceurException}, using the current {@link TraceurConfig}
     *
     * @return The exception created with the current config
     */
    public static TraceurException create() {
        return new TraceurException(Traceur.getConfig());
    }

    TraceurException(TraceurConfig config) {
        super("Debug Exception generated at call site");
        this.shouldFilterStackTraces = config.shouldFilterStackTraces();
        this.assemblyLogLevel = config.getAssemblyLogLevel();
        this.throwableConsumer = config.getThrowableListener();
        if (assemblyLogLevel != AssemblyLogLevel.NONE) {
            this.setStackTrace(createStackTrace());
        }
    }

    private void notifyThrowableIfNeeded(Throwable throwable) {
        if (throwableConsumer != null) {
            try {
                throwableConsumer.accept(throwable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Adds this as the root cause of the given exception and then it's notified to the throwable listener.
     * If the current root cause is already a {@code TraceurException}, and the log level is set to
     * {@link AssemblyLogLevel#SHOW_ONLY_FIRST}, then this will not be appended to the exception
     * If the log level is {@link AssemblyLogLevel#NONE} the throwable is not updated
     *
     * @param throwable The exception to append to
     * @return The original exception (with this appended as the root cause)
     */
    public Throwable appendAndNotify(Throwable throwable) {
        if (assemblyLogLevel == AssemblyLogLevel.NONE) {
            notifyThrowableIfNeeded(throwable);
            return throwable;
        }

        Throwable t = throwable;
        while (t.getCause() != null) {
            t = t.getCause();

            // Won't be able to init the cause of this with self
            if (t == this) {
                notifyThrowableIfNeeded(throwable);
                return throwable;
            }

            if (assemblyLogLevel == AssemblyLogLevel.SHOW_ONLY_FIRST && t instanceof TraceurException) {
                notifyThrowableIfNeeded(throwable);
                return throwable;
            }
        }

        t.initCause(this);

        notifyThrowableIfNeeded(throwable);
        return throwable;
    }

    private StackTraceElement[] createStackTrace() {
        final StackTraceElement[] realStackTrace = Thread.currentThread().getStackTrace();
        if (!shouldFilterStackTraces) {
            return realStackTrace;
        }

        final List<StackTraceElement> filtered = new ArrayList<>(realStackTrace.length);

        for (StackTraceElement element : realStackTrace) {
            if (filterLine(element)) {
                filtered.add(element);
            }
        }

        return filtered.toArray(new StackTraceElement[filtered.size()]);
    }

    /**
     * @param element Stack trace line to potentially filter
     * @return true if the element should be shown in the stacktrace
     */
    private boolean filterLine(final StackTraceElement element) {
        final String className = element.getClassName();

        // Remove references to Traceur & RxJavaPlugins
        return !(className.contains(".Traceur")
                || className.contains("OnAssembly")
                || className.endsWith(".RxJavaPlugins"));
    }
}
