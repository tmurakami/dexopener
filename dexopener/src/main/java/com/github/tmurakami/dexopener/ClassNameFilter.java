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

import java.util.HashSet;
import java.util.Set;

final class ClassNameFilter {

    private static final String[] INCLUDED_PACKAGES = {
            "android.databinding.generated.",
    };

    private static final String[] INCLUDED_CLASSES = {
            // Since the Data Binding Library generates several classes that are tightly coupled
            // with user classes, do not exclude the following classes.
            "android.databinding.DataBinderMapper",
            "android.databinding.DataBindingComponent",
            "android.databinding.DataBindingUtil",
    };

    private final String packagePrefix;
    private final Set<String> excludedClasses = new HashSet<>();

    ClassNameFilter(String packagePrefix) {
        this.packagePrefix = packagePrefix;
    }

    ClassNameFilter excludeClasses(String... classNames) {
        for (String className : classNames) {
            if (className.startsWith(packagePrefix)) {
                excludedClasses.add(className);
            }
        }
        return this;
    }

    boolean accept(String className) {
        for (String pkg : INCLUDED_PACKAGES) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        for (String cls : INCLUDED_CLASSES) {
            if (className.equals(cls)) {
                return true;
            }
        }
        return className.startsWith(packagePrefix) &&
               !className.endsWith(".BR") &&
               !className.endsWith(".BuildConfig") &&
               !className.endsWith(".R") &&
               !className.contains(".R$") &&
               !excludedClasses.contains(className);
    }

}
