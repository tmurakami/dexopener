package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationVisitor;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ClassVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader.SKIP_CODE;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader.SKIP_DEBUG;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class InternalNamesSetReader extends ApplicationVisitor {

    private static final int MAX_SIZE_PER_LIST = 150;

    private final Set<String> internalNames = new HashSet<>();
    private final ClassNameFilter classNameFilter;

    InternalNamesSetReader(ClassNameFilter classNameFilter) {
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

    Set<Set<String>> read(ApplicationReader applicationReader) {
        applicationReader.accept(this, null, SKIP_CODE | SKIP_DEBUG);
        List<String> list = new ArrayList<>(internalNames);
        internalNames.clear();
        Collections.sort(list);
        Set<Set<String>> set = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (String n : list) {
            names.add(n);
            if (names.size() == MAX_SIZE_PER_LIST) {
                set.add(Collections.unmodifiableSet(names));
                names = new HashSet<>();
            }
        }
        if (!names.isEmpty()) {
            set.add(Collections.unmodifiableSet(names));
        }
        return Collections.unmodifiableSet(set);
    }

}
