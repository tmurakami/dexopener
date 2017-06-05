package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Annotation;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.AnnotationElement;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Method;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.value.IntEncodedValue;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableAnnotation;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableAnnotationElement;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableMethod;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.value.ImmutableIntEncodedValue;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.io.FileDataStore;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.pool.DexPool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import dalvik.system.DexFile;

final class DexFilesImpl implements DexFiles {

    private final Map<String, DexFile> dexFileMap;
    private final Set<ImmutableDexFile> dexFiles;
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;

    DexFilesImpl(Map<String, DexFile> dexFileMap,
                 Set<ImmutableDexFile> dexFiles,
                 File cacheDir,
                 DexFileLoader dexFileLoader) {
        this.dexFileMap = dexFileMap;
        this.dexFiles = dexFiles;
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
    }

    @Override
    public dalvik.system.DexFile get(String className) throws IOException {
        DexFile cached = getFromCache(className);
        if (cached != null) {
            return cached;
        }
        ImmutableDexFile file = getDexFileToBeOpened(className);
        if (file == null) {
            return null;
        } else if (cacheDir.isDirectory() || cacheDir.mkdirs()) {
            return putToCache(generate(open(file)));
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

    private ImmutableDexFile getDexFileToBeOpened(String className) {
        for (Iterator<ImmutableDexFile> it = dexFiles.iterator(); it.hasNext(); ) {
            ImmutableDexFile dexFile = it.next();
            for (ClassDef def : dexFile.getClasses()) {
                if (def.getType().equals(TypeUtils.getInternalName(className))) {
                    it.remove();
                    return dexFile;
                }
            }
        }
        return null;
    }

    private DexFile generate(DexPool pool) throws IOException {
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

    private static DexPool open(ImmutableDexFile dexFile) {
        DexPool pool = new DexPool(dexFile.getOpcodes());
        for (ClassDef def : dexFile.getClasses()) {
            String type = def.getType();
            pool.internClass(new ImmutableClassDef(type,
                                                   open(def.getAccessFlags()),
                                                   def.getSuperclass(),
                                                   def.getInterfaces(),
                                                   def.getSourceFile(),
                                                   open(def.getAnnotations()),
                                                   def.getFields(),
                                                   open(def.getMethods())));
            Logger logger = Loggers.get();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Class to be opened: " + TypeUtils.getClassName(type));
            }
        }
        return pool;
    }

    private static int open(int accessFlags) {
        return accessFlags & ~Modifier.FINAL;
    }

    private static Set<Annotation> open(Set<? extends Annotation> annotations) {
        Set<Annotation> set = new HashSet<>();
        for (Annotation a : annotations) {
            String type = a.getType();
            if (type.equals("Ldalvik/annotation/InnerClass;")) {
                Set<AnnotationElement> elements = new HashSet<>();
                for (AnnotationElement e : a.getElements()) {
                    String name = e.getName();
                    if (name.equals("accessFlags")) {
                        int accessFlags = open(((IntEncodedValue) e.getValue()).getValue());
                        ImmutableIntEncodedValue value = new ImmutableIntEncodedValue(accessFlags);
                        elements.add(new ImmutableAnnotationElement(name, value));
                    } else {
                        elements.add(e);
                    }
                }
                set.add(new ImmutableAnnotation(a.getVisibility(), type, elements));
            } else {
                set.add(a);
            }
        }
        return set;
    }

    private static Set<Method> open(Iterable<? extends Method> methods) {
        Set<Method> set = new HashSet<>();
        for (Method m : methods) {
            set.add(new ImmutableMethod(m.getDefiningClass(),
                                        m.getName(),
                                        m.getParameters(),
                                        m.getReturnType(),
                                        open(m.getAccessFlags()),
                                        m.getAnnotations(),
                                        m.getImplementation()));
        }
        return set;
    }

}
