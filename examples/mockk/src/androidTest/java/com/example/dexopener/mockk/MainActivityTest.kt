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

import android.app.Activity
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest : ActivityLifecycleCallback {
    @Before
    fun setUp() = ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(this)

    @After
    fun tearDown() = ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(this)

    override fun onActivityLifecycleChanged(activity: Activity, stage: Stage) {
        if (stage == Stage.PRE_ON_CREATE) {
            (activity as MainActivity).myService = mockk(relaxUnitFun = true)
        }
    }

    @Test
    fun onCreate_should_call_MyService_doIt() {
        launchActivity<MainActivity>().use {
            it.onActivity { activity -> verify { activity.myService.doIt() } }
        }
    }
}
