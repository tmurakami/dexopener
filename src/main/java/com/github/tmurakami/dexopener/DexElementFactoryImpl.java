package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.lowLevelUtils.DexFileReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexElementFactoryImpl implements DexElementFactory {

    private final ClassNameFilter classNameFilter;
    private final DexGenerator dexGenerator;
    private final DexFileLoader fileLoader;

    DexElementFactoryImpl(ClassNameFilter classNameFilter,
                          DexGenerator dexGenerator,
                          DexFileLoader fileLoader) {
        this.classNameFilter = classNameFilter;
        this.dexGenerator = dexGenerator;
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
        ApplicationReader ar = new ApplicationReader(ASM4, in);
        Collection<String> classNames = collectClassNames((DexFileReader) ar.getDexFile(), classNameFilter);
        return new DexElementImpl(ar, classNames, cacheDir, dexGenerator, fileLoader);
    }

    private static Collection<String> collectClassNames(DexFileReader r, ClassNameFilter classNameFilter) {
        int size = r.getClassDefinitionsSize();
        List<String> names = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            r.seek(r.getClassDefinitionOffset(i));
            String name = r.getStringItemFromTypeIndex(r.uint());
            if (classNameFilter.accept(name.substring(1, name.length() - 1).replace('/', '.'))) {
                names.add(name);
            }
        }
        Collections.sort(names);
        return names;
    }

}
