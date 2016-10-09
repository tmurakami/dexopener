package com.github.tmurakami.dexopener;

final class ClassLoaderFactoryImpl implements ClassLoaderFactory {

    private final ClassNameFilter classNameFilter;

    ClassLoaderFactoryImpl(ClassNameFilter classNameFilter) {
        this.classNameFilter = classNameFilter;
    }

    @Override
    public ClassLoader newClassLoader(Iterable<String> dexPaths,
                                      String optimizedDirectory,
                                      ClassLoader parent) {
        StringBuilder builder = new StringBuilder();
        for (String path : dexPaths) {
            if (path.length() > 0) {
                builder.append(':');
            }
            builder.append(path);
        }
        return new InternalClassLoader(builder.toString(), optimizedDirectory, classNameFilter, parent);
    }

}
