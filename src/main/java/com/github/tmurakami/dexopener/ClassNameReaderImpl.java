package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.lowLevelUtils.DexFileReader;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class ClassNameReaderImpl implements ClassNameReader {

    private final ClassNameFilter classNameFilter;

    ClassNameReaderImpl(ClassNameFilter classNameFilter) {
        this.classNameFilter = classNameFilter;
    }

    @Override
    public Set<String> readClassNames(DexFileReader reader) {
        Set<String> names = new HashSet<>();
        int size = reader.getClassDefinitionsSize();
        for (int i = 0; i < size; ++i) {
            reader.seek(reader.getClassDefinitionOffset(i));
            String name = reader.getStringItemFromTypeIndex(reader.uint());
            if (classNameFilter.accept(name.substring(1, name.length() - 1).replace('/', '.'))) {
                names.add(name);
            }
        }
        return Collections.unmodifiableSet(names);
    }

}
