package com.github.tmurakami.classinjector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ClassSources implements ClassSource {

    private final List<ClassSource> sources;

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
    public byte[] getBytecodeFor(String name) throws IOException {
        for (ClassSource s : sources) {
            byte[] bytecode = s.getBytecodeFor(name);
            if (bytecode != null) {
                return bytecode;
            }
        }
        return null;
    }

}
