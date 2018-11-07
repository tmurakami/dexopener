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

import android.content.Intent
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.intercepting.SingleActivityFactory
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @[Rule JvmField]
    val activityRule =
        ActivityTestRule(object : SingleActivityFactory<MainActivity>(MainActivity::class.java) {
            override fun create(intent: Intent): MainActivity {
                return MainActivity().also { it.myService = mockk(relaxUnitFun = true) }
            }
        }, /* initialTouchMode */ false, /* launchActivity */ false)

    @Test
    fun onCreate_should_call_MyService_doIt() {
        activityRule.launchActivity(null)
        verify { activityRule.activity.myService.doIt() }
    }

}
