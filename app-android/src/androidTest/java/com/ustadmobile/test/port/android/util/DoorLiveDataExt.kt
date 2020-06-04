package com.ustadmobile.test.port.android.util

import androidx.fragment.app.testing.FragmentScenario
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
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
fun <T> DoorLiveData<T>.waitUntilWithFragmentScenario(fragmentScenario: FragmentScenario<*>,
                                                      timeout: Long = 5000, checker: (T) -> Boolean): T? {
    val completableDeferred = CompletableDeferred<T>()
    val observerFn = object : DoorObserver<T> {
        override fun onChanged(t: T) {
            if(checker.invoke(t))
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
