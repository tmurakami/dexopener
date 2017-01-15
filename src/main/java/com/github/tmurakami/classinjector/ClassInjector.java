package com.github.tmurakami.classinjector;

public abstract class ClassInjector {

    ClassInjector() {
    }

    public abstract void into(ClassLoader target);

    public static Using using(ClassDefiner definer) {
        if (definer == null) {
            throw new IllegalArgumentException("'definer' is null");
        }
        return new Using(definer, new StealthClassLoader.Factory());
    }

    public static final class Using {

        private final ClassDefiner definer;
        private final StealthClassLoader.Factory classLoaderFactory;

        Using(ClassDefiner definer, StealthClassLoader.Factory classLoaderFactory) {
            this.definer = definer;
            this.classLoaderFactory = classLoaderFactory;
        }

        public ClassInjector from(ClassSource source) {
            if (source == null) {
                throw new IllegalArgumentException("'source' is null");
            }
            return new ClassInjectorImpl(definer, source, classLoaderFactory);
        }

    }

}
