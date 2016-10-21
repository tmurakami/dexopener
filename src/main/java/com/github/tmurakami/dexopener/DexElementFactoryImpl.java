package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexElementFactoryImpl implements DexElementFactory {

    private final ClassNameFilter classNameFilter;
    private final DexFileLoader fileLoader;

    DexElementFactoryImpl(ClassNameFilter classNameFilter, DexFileLoader fileLoader) {
        this.classNameFilter = classNameFilter;
        this.fileLoader = fileLoader;
    }

    @Override
    public DexElement newDexElement(File file, File cacheDir) {
        List<DexElement> elements = new ArrayList<>();
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            ZipEntry e = zipFile.getEntry("classes.dex");
            if (e == null) {
                throw new Error(file + " does not contain the classes.dex");
            }
            elements.add(newDexElement(zipFile.getInputStream(e), cacheDir));
            for (int i = 2; (e = zipFile.getEntry("classes" + i + ".dex")) != null; i++) {
                elements.add(newDexElement(zipFile.getInputStream(e), cacheDir));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(zipFile);
        }
        return new DexElements(elements);
    }

    private DexElement newDexElement(InputStream in, File cacheDir) throws IOException {
        return new DexElementImpl(new ApplicationReader(ASM4, in), cacheDir, classNameFilter, fileLoader);
    }

}
