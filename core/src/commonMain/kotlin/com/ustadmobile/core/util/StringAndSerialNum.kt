package com.ustadmobile.core.util

import kotlinx.atomicfu.atomic

/**
 * The error message on a textfield should normally be cleared as soon as the user types again.
 *
 * We don't want to use separate event handlers for each property which would
 * otherwise be needed e.g. onFirstNameChanged, onLastNameChanged etc.
 *
 * The ViewModel can send a StringAndSerialNum object. By default each new object has a new serial
 * number.
 *
 * Composables and React components can use an internal state that is invalidated when an error
 * message with a new serial number is received. This ensures that the error message will re-appear if
 * that is what the ViewModel wants (e.g. the user types an invalid site link, clicks
 * next, then the view model sets the error message to "invalid link". The user types again, the
 * error message is cleared in the internal state of the composable/react component. The link is
 * still invalid, so the viewmodel sends a new StringAndSerialNum object. This can be recognized by
 * the Composable/React as a new value, and then the error text can be displayed. Using only the
 * error message itself is not sufficient, because it might be that the same error message will be
 * displayed again.
 */
data class StringAndSerialNum(
    val message: String, val serial: Int = atomicInt.incrementAndGet(),
) {

    override fun toString(): String {
        return message
    }

    companion object {

        private val atomicInt = atomic(0)

    }
}
