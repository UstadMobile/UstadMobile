package com.ustadmobile.test.port.android.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ActivityScenario
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.lifecycle.Observer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/**
 * This is a utility function that makes it easy to wait until a LiveData object meets a given
 * condition. This can be useful in testing (e.g. wait for a value to be saved to the database).
 *
 * The general waitUntil function will not work because Android requires that adding and removing
 * an observer must take place on the main thread, and the test function is not a coroutine.
 *
 */
fun <T, F: Fragment> LiveData<T>.waitUntilWithFragmentScenario(fragmentScenario: FragmentScenario<F>,
                                                                   timeout: Long = 5000, checker: (T) -> Boolean): T? {
    val completableDeferred = CompletableDeferred<T>()
    val observerFn = object : Observer<T> {
        override fun onChanged(t: T) {
            if (checker.invoke(t))
                completableDeferred.complete(t)
        }
    }

    fragmentScenario.onFragment {
        observeForever(observerFn)
    }

    runBlocking {
        withTimeoutOrNull(timeout) { completableDeferred.await() }
    }

    return fragmentScenario.letOnFragment {
        removeObserver(observerFn)
        value
    }
}

fun <T> LiveData<T>.waitUntilWithActivityScenario(activityScenario: ActivityScenario<*>,
                                                      timeout: Long = 5000, checker: (T) -> Boolean): T? {
    val completableDeferred = CompletableDeferred<T>()
    val observerFn = object : Observer<T> {
        override fun onChanged(t: T) {
            if (checker.invoke(t))
                completableDeferred.complete(t)
        }
    }

    activityScenario.onActivity {
        observeForever(observerFn)
    }

    runBlocking {
        withTimeoutOrNull(timeout) { completableDeferred.await() }
    }

    activityScenario.onActivity {
        removeObserver(observerFn)
    }

    return value
}

