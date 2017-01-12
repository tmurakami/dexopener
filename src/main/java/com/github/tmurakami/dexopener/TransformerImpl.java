package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class TransformerImpl implements Transformer {

    private final ApplicationReader applicationReader;

    TransformerImpl(ApplicationReader applicationReader) {
        this.applicationReader = applicationReader;
    }

    @Override
    public byte[] transform(String[] classNames) {
        ApplicationWriter aw = new ApplicationWriter();
        applicationReader.accept(new ApplicationOpener(aw), classNames, 0);
        return aw.toByteArray();
    }

    static final class FactoryImpl implements Factory {
        @Override
        public Transformer newTransformer(byte[] dexBytes) {
            return new TransformerImpl(new ApplicationReader(ASM4, dexBytes));
        }
    }

}
