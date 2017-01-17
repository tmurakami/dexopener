package com.github.tmurakami.classinjector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An object representing a group of {@link ClassSource} objects.
 */
public final class ClassSources implements ClassSource {

    private final List<ClassSource> sources;

    /**
     * Create an instance.
     *
     * @param sources The list of non-null {@link ClassSource} objects
     */
    public ClassSources(Iterable<? extends ClassSource> sources) {
        if (sources == null) {
            throw new IllegalArgumentException("'sources' is null");
        }
        List<ClassSource> list = new ArrayList<ClassSource>();
        for (ClassSource s : sources) {
            if (s == null) {
                throw new IllegalArgumentException("'sources' contains null");
            }
            list.add(s);
        }
        this.sources = list;
    }

    @Override
    public ClassFile getClassFile(String className) throws IOException {
        for (ClassSource b : sources) {
            ClassFile f = b.getClassFile(className);
            if (f != null) {
                return f;
            }
        }
        return null;
    }

}
