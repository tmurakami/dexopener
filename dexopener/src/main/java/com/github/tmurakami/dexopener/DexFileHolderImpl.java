package com.github.tmurakami.dexopener;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@SuppressWarnings("deprecation")
final class DexFileHolderImpl implements DexFileHolder {

    private FutureTask<dalvik.system.DexFile> task;

    void setTask(FutureTask<dalvik.system.DexFile> task) {
        this.task = task;
    }

    @Override
    public dalvik.system.DexFile get() throws IOException {
        // The task might not be completed, so we do it here first.
        task.run();
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return task.get();
                } catch (InterruptedException e) {
                    // Refuse to be interrupted
                    interrupted = true;
                }
            }
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                throw new UndeclaredThrowableException(e, "Unexpected error");
            }
        } finally {
            if (interrupted) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
            }
        }
    }

}
