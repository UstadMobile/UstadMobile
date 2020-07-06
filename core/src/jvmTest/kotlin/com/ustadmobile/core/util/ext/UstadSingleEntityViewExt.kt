package com.ustadmobile.core.util.ext

import com.nhaarman.mockitokotlin2.nullableArgumentCaptor
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.view.UstadSingleEntityView

/**
 * This is a convenience function for use with UstadEditView and UstadDetailView. It will return the
 * last value of entity that was set on the view. If no value for entity has been set, it will wait
 * up to timeoutMillis
 *
 * @param timeoutMillis A timeout for the amount of time to wait for the entity to be set
 * @return The last value of the entity that was set on this view
 */
inline fun <reified RT : Any> UstadSingleEntityView<RT>.captureLastEntityValue(timeoutMillis: Long = 5000) : RT? {
    return nullableArgumentCaptor<RT>().run {
        verify(this@captureLastEntityValue, timeout(timeoutMillis).atLeastOnce()).entity = capture()
        firstValue
    }
}
