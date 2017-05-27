package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationVisitor;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ClassVisitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader.SKIP_CODE;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader.SKIP_DEBUG;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class InternalNameReader extends ApplicationVisitor {

    private final Set<String> internalNames = new HashSet<>();
    private final ClassNameFilter classNameFilter;

    InternalNameReader(ClassNameFilter classNameFilter) {
        super(ASM4);
        this.classNameFilter = classNameFilter;
    }

    @Override
    public ClassVisitor visitClass(int access,
                                   String name,
                                   String[] signature,
                                   String superName,
                                   String[] interfaces) {
        if (classNameFilter.accept(DexUtils.toClassName(name))) {
            internalNames.add(name);
        }
        return null;
    }

    Set<String> read(ApplicationReader applicationReader) {
        applicationReader.accept(this, null, SKIP_CODE | SKIP_DEBUG);
        Set<String> set = new HashSet<>(internalNames);
        internalNames.clear();
        return Collections.unmodifiableSet(set);
    }

}
