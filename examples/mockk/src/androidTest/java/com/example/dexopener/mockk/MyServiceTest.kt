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

package com.example.dexopener.mockk

import android.os.Build
import android.util.Log
import androidx.test.filters.SdkSuppress
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.Test

class MyServiceTest {
    // Stubbing static methods is supported only on Android P+.
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.P)
    @Test
    fun doIt_should_log_some_debug_message() {
        mockkStatic(Log::class)
        try {
            every { Log.d(any(), any()) } returns 0
            MyService().doIt()
            verify { Log.d("MyService", "some debug message") }
        } finally {
            unmockkStatic(Log::class)
        }
    }
}
