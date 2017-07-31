package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassInjector;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

final class ClassInjectorFactory {
    ClassInjector newClassInjector(ClassSource classSource) {
        return ClassInjector.from(classSource);
    }
}
