package com.github.tmurakami.dexopener;

import android.support.annotation.NonNull;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;
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

@SuppressWarnings("deprecation")
final class DexClassSource implements ClassSource {

    private final Opcodes opcodes;
    private final Map<String, dalvik.system.DexFile> dexFileMap;
    private final Set<Set<ClassDef>> classesSet;
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;
    private final DexClassFileFactory dexClassFileFactory;

    DexClassSource(Opcodes opcodes,
                   Map<String, dalvik.system.DexFile> dexFileMap,
                   Set<Set<ClassDef>> classesSet,
                   File cacheDir,
                   DexFileLoader dexFileLoader,
                   DexClassFileFactory dexClassFileFactory) {
        this.opcodes = opcodes;
        this.dexFileMap = dexFileMap;
        this.classesSet = classesSet;
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
        this.dexClassFileFactory = dexClassFileFactory;
    }

    @Override
    public ClassFile getClassFile(@NonNull String className) throws IOException {
        dalvik.system.DexFile dexFile = getDexFile(className);
        return dexFile == null ? null : dexClassFileFactory.newClassFile(className, dexFile);
    }

    private dalvik.system.DexFile getDexFile(String className) throws IOException {
        dalvik.system.DexFile dexFile = getFromCache(className);
        if (dexFile != null) {
            return dexFile;
        }
        Set<ClassDef> classes = getClassesToBeOpened(className);
        if (classes.isEmpty()) {
            return null;
        }
        DexPool pool = openClasses(classes);
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            throw new IOException("Cannot create " + cacheDir);
        }
        File dex = File.createTempFile("classes", ".dex", cacheDir);
        try {
            pool.writeTo(new FileDataStore(dex));
            String sourcePathName = dex.getCanonicalPath();
            String outputPathName = sourcePathName + ".opt";
            dexFile = dexFileLoader.loadDex(sourcePathName, outputPathName, 0);
            Logger logger = Loggers.get();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("The DEX file for " + className + " was generated: " + outputPathName);
            }
        } finally {
            FileUtils.delete(dex);
        }
        return putToCache(dexFile);
    }

    private dalvik.system.DexFile getFromCache(String className) {
        dalvik.system.DexFile dexFile = dexFileMap.get(className);
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

    private dalvik.system.DexFile putToCache(dalvik.system.DexFile dexFile) {
        for (Enumeration<String> e = dexFile.entries(); e.hasMoreElements(); ) {
            dexFileMap.put(e.nextElement(), dexFile);
        }
        return dexFile;
    }

    private static ClassDef openClass(ClassDef def) {
        return new ImmutableClassDef(def.getType(),
                                     removeFinalModifier(def.getAccessFlags()),
                                     def.getSuperclass(),
                                     def.getInterfaces(),
                                     def.getSourceFile(),
                                     openInnerClassAnnotation(def.getAnnotations()),
                                     def.getFields(),
                                     openMethods(def.getMethods()));
    }

    private static int removeFinalModifier(int accessFlags) {
        return accessFlags & ~Modifier.FINAL;
    }

    private static Set<Annotation> openInnerClassAnnotation(Set<? extends Annotation> annotations) {
        Set<Annotation> openedAnnotations = new HashSet<>();
        for (Annotation a : annotations) {
            String type = a.getType();
            if (type.equals("Ldalvik/annotation/InnerClass;")) {
                Set<AnnotationElement> elementSet = new HashSet<>();
                for (AnnotationElement e : a.getElements()) {
                    String name = e.getName();
                    if (name.equals("accessFlags")) {
                        IntEncodedValue value = (IntEncodedValue) e.getValue();
                        int accessFlags = removeFinalModifier(value.getValue());
                        IntEncodedValue newValue = new ImmutableIntEncodedValue(accessFlags);
                        elementSet.add(new ImmutableAnnotationElement(name, newValue));
                    } else {
                        elementSet.add(e);
                    }
                }
                openedAnnotations.add(new ImmutableAnnotation(a.getVisibility(), type, elementSet));
            } else {
                openedAnnotations.add(a);
            }
        }
        return openedAnnotations;
    }

    private static Set<Method> openMethods(Iterable<? extends Method> methods) {
        Set<Method> openedMethods = new HashSet<>();
        for (Method m : methods) {
            openedMethods.add(new ImmutableMethod(m.getDefiningClass(),
                                                  m.getName(),
                                                  m.getParameters(),
                                                  m.getReturnType(),
                                                  removeFinalModifier(m.getAccessFlags()),
                                                  m.getAnnotations(),
                                                  m.getImplementation()));
        }
        return openedMethods;
    }

}
