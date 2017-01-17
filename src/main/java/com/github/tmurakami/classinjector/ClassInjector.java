package com.github.tmurakami.classinjector;

/**
 * An object that injects classes into a class loader.
 */
public abstract class ClassInjector {

    ClassInjector() {
    }

    /**
     * Inject classes into the given class loader.
     *
     * @param target The {@link ClassLoader} to be injected with classes.
     */
    public abstract void into(ClassLoader target);

    /**
     * Create an instance of {@link ClassInjector}.
     *
     * @param source The input source of data that make up a class
     * @return The {@link ClassInjector} object for injecting classes constructed using the
     * specified source into a class loader.
     */
    public static ClassInjector from(ClassSource source) {
        if (source == null) {
            throw new IllegalArgumentException("'source' is null");
        }
        return new ClassInjectorImpl(source, StealthClassLoader.Factory.INSTANCE);
    }

}
