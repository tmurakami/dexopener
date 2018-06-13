package com.github.tmurakami.dexopener;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;

@SuppressWarnings("deprecation")
final class DexFileHolderImpl implements DexFileHolder {

    private RunnableFuture<dalvik.system.DexFile> dexFileFuture;

    void setDexFileFuture(RunnableFuture<dalvik.system.DexFile> dexFileFuture) {
        this.dexFileFuture = dexFileFuture;
    }

    @Override
    public dalvik.system.DexFile get() throws IOException {
        // The future might not be completed, so we do it here first.
        dexFileFuture.run();
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return dexFileFuture.get();
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
                throw new UndeclaredThrowableException(cause, "Unexpected error");
            }
        } finally {
            if (interrupted) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
            }
        }
    }

}
