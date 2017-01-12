package com.github.tmurakami.dexopener;

interface ClassNameFilter {

    ClassNameFilter ALL = new ClassNameFilter() {
        @Override
        public boolean accept(String name) {
            return true;
        }
    };

    boolean accept(String name);

}
