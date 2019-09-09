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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

final class FileUtils {

    private FileUtils() {
        throw new AssertionError("Do not instantiate");
    }

    static void delete(File... files) {
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                delete(f.listFiles());
            }
            if (f.exists() && !f.delete()) {
                Logger logger = Loggers.get();
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning("Could not delete " + f);
                }
            }
        }
    }

}
