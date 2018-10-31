/*
 * Copyright 2016 Tsuyoshi Murakami
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tmurakami.dexopener;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;

@SuppressWarnings("deprecation")
final class DexClassSource implements ClassSource {

    private final Map<String, RunnableFuture<dalvik.system.DexFile>> futureMap;

    DexClassSource(Map<String, RunnableFuture<dalvik.system.DexFile>> futureMap) {
        this.futureMap = futureMap;
    }

    @Override
    public ClassFile getClassFile(String className) throws IOException {
        RunnableFuture<dalvik.system.DexFile> future = futureMap.get(className);
        if (future == null) {
            return null;
        }
        // The future might not be completed, so we do it here first.
        future.run();
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return new ClassFile(className, future.get());
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
