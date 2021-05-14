package ru.sberdevices.common.camera

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.sberdevices.camera.view.CameraView

@RunWith(AndroidJUnit4::class)
class CameraViewTest {
    @get:Rule
    var activityRule: ActivityScenarioRule<TestActivity> = ActivityScenarioRule(
        TestActivity::class.java
    )

    @Test
    fun check_uses_cameraId_from_arguments() {
        onView(withId(ru.sberdevices.camera.test.R.id.cameraView0))
            .check { view, _ ->
                requireNotNull(view as CameraView)
                assertEquals(view.cameraId, "0")
            }

        onView(withId(ru.sberdevices.camera.test.R.id.cameraView1))
            .check { view, _ ->
                requireNotNull(view as CameraView)
                assertEquals(view.cameraId, "1")
            }
    }
}
