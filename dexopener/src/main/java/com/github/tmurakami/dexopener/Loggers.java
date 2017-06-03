package com.github.tmurakami.dexopener;

import java.util.logging.Logger;

final class Loggers {

    private Loggers() {
        throw new AssertionError("Do not instantiate");
    }

    static Logger get() {
        return Logger.getLogger(BuildConfig.APPLICATION_ID);
    }

}
