package com.ustadmobile.test.port.android.util

import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onIdle
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.mockito.kotlin.*
import java.util.concurrent.atomic.AtomicReference

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

@Deprecated("This should have a delay / sleep ")
fun <F: Fragment, R> FragmentScenario<F>.waitUntilLetOnFragment(block: (F) -> R) : R{
    var retVal: R? = null
    while(retVal == null){
        onFragment {
            retVal = block(it)
        }
    }

    return retVal as R
}

suspend fun <F: Fragment, R> FragmentScenario<F>.waitUntilOnFragment(
    timeout: Long,
    getter: (F) -> R?,
    checker: (R?) -> Boolean
) : R? {

    val retVal: AtomicReference<R?> = AtomicReference()
    withTimeout(timeout) {
        do {
            onFragment {
                retVal.set(getter(it))
            }

            if(checker(retVal.get()))
                break

            delay(50)
        }while(true)
    }

    return retVal.get()
}
suspend fun <F: Fragment, R> FragmentScenario<F>.waitUntilNotNullOnFragment(
    timeout: Long,
    getter: (F) -> R?
) : R {
    return waitUntilOnFragment(timeout, getter) { it != null}
        ?: throw IllegalStateException("Waituntilnotnull was null")
}

fun <F: Fragment, R> FragmentScenario<F>.waitUntilNotNullOnFragmentBlocking(
    timeout: Long,
    getter: (F) -> R?
) : R {
    return runBlocking {
        waitUntilOnFragment(timeout, getter) { it != null }
            ?: throw IllegalStateException("Waituntilnotnull was null")
    }
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
