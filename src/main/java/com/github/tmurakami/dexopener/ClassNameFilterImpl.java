package com.github.tmurakami.dexopener;

final class ClassNameFilterImpl implements ClassNameFilter {

    private static final String[] IGNORED_PACKAGES = {
            "com.github.tmurakami.dexmockito.",
            "net.bytebuddy.",
            "org.mockito.",
            "org.objenesis.",
    };

    @Override
    public boolean accept(String name) {
        for (String pkg : IGNORED_PACKAGES) {
            if (name.startsWith(pkg)) {
                return false;
            }
        }
        return !name.endsWith(".R") && !name.contains(".R$");
    }

}
