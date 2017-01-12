package com.github.tmurakami.dexopener;

interface Transformer {

    byte[] transform(String[] classNames);

    interface Factory {
        Transformer newTransformer(byte[] dexBytes);
    }

}
