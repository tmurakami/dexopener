package com.github.tmurakami.classinjector;

import java.io.IOException;

public interface ClassSource {
    byte[] getBytecodeFor(String name) throws IOException;
}
