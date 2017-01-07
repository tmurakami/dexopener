package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.lowLevelUtils.DexFileReader;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class ClassNameReaderImpl implements ClassNameReader {

    private static final int MAX_CLASSES_PER_DEX_FILE = 100;

    private final ClassNameFilter classNameFilter;

    ClassNameReaderImpl(ClassNameFilter classNameFilter) {
        this.classNameFilter = classNameFilter;
    }

    @Override
    public Set<Set<String>> read(DexFileReader reader) {
        Set<Set<String>> classNamesSet = new HashSet<>();
        Set<String> names = new HashSet<>();
        int classDefinitionsSize = reader.getClassDefinitionsSize();
        for (int i = 0; i < classDefinitionsSize; ++i) {
            reader.seek(reader.getClassDefinitionOffset(i));
            String name = reader.getStringItemFromTypeIndex(reader.uint());
            if (classNameFilter.accept(name.substring(1, name.length() - 1).replace('/', '.'))) {
                names.add(name);
                if (names.size() >= MAX_CLASSES_PER_DEX_FILE) {
                    classNamesSet.add(names);
                    names = new HashSet<>();
                }
            }
        }
        classNamesSet.add(names);
        return Collections.unmodifiableSet(classNamesSet);
    }

}
