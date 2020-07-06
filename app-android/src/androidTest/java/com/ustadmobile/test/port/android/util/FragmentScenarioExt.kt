package com.ustadmobile.test.port.android.util

import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onIdle
import com.nhaarman.mockitokotlin2.mock

/**
 * Convenience function that will run the given code block on a fragment and return the result as a
 * capture.
 */
fun <F: Fragment, R> FragmentScenario<F>.letOnFragment(block: (F) -> R) : R{
    var retVal: R? = null
    onFragment {
        retVal = block(it)
    }

    return retVal!!
}

/**
 * Convenience function that will run the given code block on a fragment and return the result as a
 * capture (with a nullable result)
 */
fun <F: Fragment, R> FragmentScenario<F>.nullableLetOnFragment(block: (F) -> R) : R?{
    var retVal: R? = null
    onFragment {
        retVal = block(it)
    }

    return retVal
}

/**
 * Convenience extension function that will call on the onOptionItemSelected with the given
 * optionId
 */
fun <F: Fragment> FragmentScenario<F>.clickOptionMenu(clickOptionId: Int) {
    val menuItem = mock<MenuItem> {
        on { itemId }.thenReturn(clickOptionId)
    }

    onIdle()

    onFragment {
        it.onOptionsItemSelected(menuItem)
    }
}
