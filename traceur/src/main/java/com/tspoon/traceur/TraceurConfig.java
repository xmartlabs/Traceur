package com.tspoon.traceur;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Consumer;

public class TraceurConfig {
    private final boolean shouldFilterStackTraces;
    private final Traceur.AssemblyLogLevel assemblyLogLevel;
    private final Consumer<Throwable> throwableListener;

    public TraceurConfig(boolean shouldFilterStackTraces) {
        this(shouldFilterStackTraces, null);
    }

    public TraceurConfig(boolean shouldFilterStackTraces, @Nullable Consumer<Throwable> throwableListener) {
        this.shouldFilterStackTraces = shouldFilterStackTraces;
        this.assemblyLogLevel = Traceur.AssemblyLogLevel.SHOW_ALL;
        this.throwableListener = throwableListener;
    }

    boolean shouldFilterStackTraces() {
        return shouldFilterStackTraces;
    }

    @NonNull
    Traceur.AssemblyLogLevel getAssemblyLogLevel() {
        return assemblyLogLevel;
    }

    @Nullable
    Consumer<Throwable> getThrowableListener() {
        return throwableListener;
    }
}
