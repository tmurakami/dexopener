package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import java.io.File;
import java.io.IOException;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexClassSource implements ClassSource {

    private final ApplicationReader applicationReader;
    private final File cacheDir;
    private final DexBytesClassFileFactory classFileFactory;

    DexClassSource(ApplicationReader applicationReader,
                   File cacheDir,
                   DexBytesClassFileFactory classFileFactory) {
        this.applicationReader = applicationReader;
        this.cacheDir = cacheDir;
        this.classFileFactory = classFileFactory;
    }

    @Override
    public ClassFile getClassFile(String className) throws IOException {
        ApplicationWriter aw = new ApplicationWriter();
        String[] classesToVisit = {'L' + className.replace('.', '/') + ';'};
        applicationReader.accept(new ApplicationOpener(aw), classesToVisit, 0);
        byte[] bytes = aw.toByteArray();
        return bytes == null ? null : classFileFactory.create(className, bytes, cacheDir);
    }

    static final class Factory {

        private final File cacheDir;

        Factory(File cacheDir) {
            this.cacheDir = cacheDir;
        }

        ClassSource create(byte[] bytes) {
            return new DexClassSource(new ApplicationReader(ASM4, bytes), cacheDir, DexBytesClassFileFactory.INSTANCE);
        }

    }

}
