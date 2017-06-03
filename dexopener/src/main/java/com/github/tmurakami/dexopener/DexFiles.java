package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import dalvik.system.DexFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexFiles {

    private byte[] bytecode;
    private final Map<String, DexFile> dexFileMap;
    private final Set<Set<String>> internalNamesSet;
    private final DexFileGenerator dexFileGenerator;

    DexFiles(byte[] bytecode,
             Map<String, DexFile> dexFileMap,
             Set<Set<String>> internalNamesSet,
             DexFileGenerator dexFileGenerator) {
        this.bytecode = bytecode;
        this.dexFileMap = dexFileMap;
        this.internalNamesSet = internalNamesSet;
        this.dexFileGenerator = dexFileGenerator;
    }

    DexFile get(String className) throws IOException {
        DexFile dexFile = getFromCache(className);
        if (dexFile != null) {
            return dexFile;
        }
        try {
            String[] classesToVisit = getClassesToBeOpened(className);
            if (classesToVisit == null) {
                return null;
            }
            byte[] bytecode = open(classesToVisit);
            return bytecode == null ? null : putToCache(dexFileGenerator.generateDex(bytecode));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Error while processing the class '" + className + "'", e);
        } finally {
            maybeReleaseBytecodeReference();
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

    private String[] getClassesToBeOpened(String className) {
        String internalName = TypeUtils.getInternalName(className);
        for (Iterator<Set<String>> it = internalNamesSet.iterator(); it.hasNext(); ) {
            Set<String> set = it.next();
            if (set.contains(internalName)) {
                it.remove();
                return set.toArray(new String[set.size()]);
            }
        }
        return null;
    }

    private byte[] open(String[] classesToBeOpened) {
        if (bytecode == null) {
            return null;
        }
        ApplicationReader ar = new ApplicationReader(ASM4, bytecode);
        ApplicationWriter aw = new ApplicationWriter();
        ar.accept(new ApplicationOpener(aw), classesToBeOpened, 0);
        return aw.toByteArray();
    }

    private void maybeReleaseBytecodeReference() {
        if (bytecode != null && internalNamesSet.isEmpty()) {
            bytecode = null;
            Logger logger = Loggers.get();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("The bytecode reference has been released because all the classes were opened");
            }
        }
    }

    private DexFile putToCache(DexFile dexFile) {
        for (Enumeration<String> e = dexFile.entries(); e.hasMoreElements(); ) {
            dexFileMap.put(e.nextElement(), dexFile);
        }
        return dexFile;
    }

}
