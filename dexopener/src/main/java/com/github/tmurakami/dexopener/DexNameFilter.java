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

import com.github.tmurakami.dexopener.repackaged.com.google.common.base.Predicate;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.analysis.reflection.util.ReflectionUtils;

import java.util.HashSet;
import java.util.Set;

final class DexNameFilter implements Predicate<String> {

    private static final String[] INCLUDED_DEX_NAME_PREFIXES = {
            "Landroid/databinding/generated/",
    };

    private static final String[] INCLUDED_DEX_NAMES = {
            // Since the Data Binding Library generates several classes that are tightly coupled
            // with user classes, do not exclude the following classes.
            "Landroid/databinding/DataBinderMapper;",
            "Landroid/databinding/DataBindingComponent;",
            "Landroid/databinding/DataBindingUtil;",
    };

    private final String includedDexNamePrefix;
    private final Set<String> excludedDexNames;

    DexNameFilter(String rootPackage, Class... excludedClasses) {
        String packagePrefix = 'L' + rootPackage.replace('.', '/') + '/';
        this.includedDexNamePrefix = packagePrefix;
        Set<String> dexNames = new HashSet<>();
        for (Class c : excludedClasses) {
            String dexName = ReflectionUtils.javaToDexName(c.getName());
            if (dexName.startsWith(packagePrefix)) {
                dexNames.add(dexName);
            }
        }
        this.excludedDexNames = dexNames;
    }

    @Override
    public boolean apply(String dexName) {
        for (String pkg : INCLUDED_DEX_NAME_PREFIXES) {
            if (dexName.startsWith(pkg)) {
                return true;
            }
        }
        for (String cls : INCLUDED_DEX_NAMES) {
            if (dexName.equals(cls)) {
                return true;
            }
        }
        return dexName.startsWith(includedDexNamePrefix) &&
               !dexName.endsWith("/BR;") &&
               !dexName.endsWith("/BuildConfig;") &&
               !dexName.endsWith("/R;") &&
               !dexName.contains("/R$") &&
               !excludedDexNames.contains(dexName);
    }

}
