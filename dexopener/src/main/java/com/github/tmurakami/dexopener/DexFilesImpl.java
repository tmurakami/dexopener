package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Annotation;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.AnnotationElement;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Method;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.value.IntEncodedValue;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableAnnotation;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableAnnotationElement;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableMethod;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.value.ImmutableIntEncodedValue;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.io.FileDataStore;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.pool.DexPool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import dalvik.system.DexFile;

final class DexFilesImpl implements DexFiles {

    private final Opcodes opcodes;
    private final Map<String, DexFile> dexFileMap;
    private final Set<Set<ClassDef>> classesSet;
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;

    DexFilesImpl(Opcodes opcodes,
                 Map<String, DexFile> dexFileMap,
                 Set<Set<ClassDef>> classesSet,
                 File cacheDir,
                 DexFileLoader dexFileLoader) {
        this.opcodes = opcodes;
        this.dexFileMap = dexFileMap;
        this.classesSet = classesSet;
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
    }

    @Override
    public dalvik.system.DexFile get(String className) throws IOException {
        DexFile cached = getFromCache(className);
        if (cached != null) {
            return cached;
        }
        Set<ClassDef> classes = getClassesToBeOpened(className);
        if (classes.isEmpty()) {
            return null;
        } else if (cacheDir.isDirectory() || cacheDir.mkdirs()) {
            return putToCache(generateDex(openClasses(classes)));
        } else {
            throw new IllegalStateException("Cannot create " + cacheDir);
        }
    }

    private DexFile getFromCache(String className) {
        DexFile dexFile = dexFileMap.get(className);
        if (dexFile == null) {
            return null;
        }
        Logger logger = Loggers.get();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("The DEX file for " + className + " was found in the cache");
        }
        return dexFile;
    }

    private Set<ClassDef> getClassesToBeOpened(String className) {
        for (Iterator<Set<ClassDef>> it = classesSet.iterator(); it.hasNext(); ) {
            Set<ClassDef> classes = it.next();
            for (ClassDef def : classes) {
                if (def.getType().equals(TypeUtils.getInternalName(className))) {
                    it.remove();
                    return classes;
                }
            }
        }
        return Collections.emptySet();
    }

    private DexFile generateDex(DexPool pool) throws IOException {
        File dex = File.createTempFile("classes", ".dex", cacheDir);
        try {
            pool.writeTo(new FileDataStore(dex));
            String sourcePathName = dex.getCanonicalPath();
            String outputPathName = sourcePathName + ".opt";
            DexFile dexFile = dexFileLoader.loadDex(sourcePathName, outputPathName, 0);
            Logger logger = Loggers.get();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("An optimized DEX file generated: " + outputPathName);
            }
            return dexFile;
        } finally {
            FileUtils.delete(dex);
        }
    }

    private DexFile putToCache(DexFile dexFile) {
        for (Enumeration<String> e = dexFile.entries(); e.hasMoreElements(); ) {
            dexFileMap.put(e.nextElement(), dexFile);
        }
        return dexFile;
    }

    private DexPool openClasses(Set<ClassDef> classes) {
        DexPool pool = new DexPool(opcodes);
        for (ClassDef def : classes) {
            pool.internClass(openClass(def));
            Logger logger = Loggers.get();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Class to be opened: " + TypeUtils.getClassName(def.getType()));
            }
        }
        return pool;
    }

    private ClassDef openClass(ClassDef def) {
        Set<Annotation> annotations = new HashSet<>();
        for (Annotation a : def.getAnnotations()) {
            annotations.add(openInnerClassAnnotation(a));
        }
        Set<Method> methods = new HashSet<>();
        for (Method m : def.getMethods()) {
            methods.add(new ImmutableMethod(m.getDefiningClass(),
                                            m.getName(),
                                            m.getParameters(),
                                            m.getReturnType(),
                                            removeFinalModifier(m.getAccessFlags()),
                                            m.getAnnotations(),
                                            m.getImplementation()));
        }
        return new ImmutableClassDef(def.getType(),
                                     removeFinalModifier(def.getAccessFlags()),
                                     def.getSuperclass(),
                                     def.getInterfaces(),
                                     def.getSourceFile(),
                                     annotations,
                                     def.getFields(),
                                     methods);
    }

    private static int removeFinalModifier(int accessFlags) {
        return accessFlags & ~Modifier.FINAL;
    }

    private static Annotation openInnerClassAnnotation(Annotation annotation) {
        String type = annotation.getType();
        if (!type.equals("Ldalvik/annotation/InnerClass;")) {
            return annotation;
        }
        Set<AnnotationElement> elements = new HashSet<>();
        for (AnnotationElement e : annotation.getElements()) {
            String name = e.getName();
            if (name.equals("accessFlags")) {
                IntEncodedValue value = (IntEncodedValue) e.getValue();
                int accessFlags = removeFinalModifier(value.getValue());
                ImmutableIntEncodedValue newValue = new ImmutableIntEncodedValue(accessFlags);
                elements.add(new ImmutableAnnotationElement(name, newValue));
            } else {
                elements.add(e);
            }
        }
        return new ImmutableAnnotation(annotation.getVisibility(), type, elements);
    }

}
