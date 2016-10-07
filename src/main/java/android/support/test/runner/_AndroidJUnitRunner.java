package android.support.test.runner;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.internal.runner.TestRequestBuilder;

public class _AndroidJUnitRunner extends AndroidJUnitRunner {

    protected _AndroidJUnitRunner() {
    }

    @Override
    protected TestRequestBuilder createTestRequestBuilder(Instrumentation instr, Bundle arguments) {
        return super.createTestRequestBuilder(instr, arguments);
    }

}
