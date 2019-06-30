/*
 * Copyright 2016 Tsuyoshi Murakami
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.dexopener.dexmaker;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

import static com.android.dx.mockito.inline.extended.ExtendedMockito.mockitoSession;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.verify;

@RunWith(AndroidJUnit4.class)
public class MyServiceTest {
    // This test uses unstable APIs. See the Javadoc of `ExtendedMockito`.
    @Test
    public void doIt_should_log_some_debug_message() {
        MockitoSession session = mockitoSession().mockStatic(Log.class)
                                                 .strictness(Strictness.STRICT_STUBS)
                                                 .startMocking();
        try {
            new MyService().doIt();
            verify(() -> Log.d("MyService", "some debug message"));
        } finally {
            session.finishMocking();
        }
    }
}
