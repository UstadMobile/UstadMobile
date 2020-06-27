package com.ustadmobile.test.port.android.util

import android.app.Activity
import android.view.MenuItem
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
import com.nhaarman.mockitokotlin2.mock

fun <A: Activity> ActivityScenario<A>.clickOptionMenu(clickOptionId: Int) {
    val menuItem = mock<MenuItem> {
        on { itemId }.thenReturn(clickOptionId)
    }
    onIdle()
    onActivity {
        it.onOptionsItemSelected(menuItem)
    }
}
