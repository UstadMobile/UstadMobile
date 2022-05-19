package com.ustadmobile.core.util

import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import org.mockito.kotlin.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * mockView will create a mock with simple stubbing for the loading field to remember its value
 */
inline fun <reified T : UstadView> mockView(
    stubbing: KStubbing<T>.(T) -> Unit
) : T {

    val loadingAtomic = AtomicBoolean(false)

    val mock = mock<T> {
        on {
            loading
        }.thenAnswer { loadingAtomic.get() }
        on {
            loading = any()
        }.thenAnswer {
            loadingAtomic.set(it.arguments[0] as Boolean)
        }
    }

    return mock.stub(stubbing)
}

/**
 * mockEditView will create a mock with simple stubbing for the loading and fieldsEnabled fields on
 * the view to remember their values.
 */
inline fun <reified T : UstadEditView<*>> mockEditView(
    stubbing: KStubbing<T>.(T) -> Unit
) : T {
    val mock = mockView(stubbing)
    val fieldsEnabledAtomic = AtomicBoolean(true)


    return mock.stub {
        on {
            fieldsEnabled
        }.thenAnswer { fieldsEnabledAtomic.get() }

        on {
            fieldsEnabled = any()
        }.thenAnswer {
            fieldsEnabledAtomic.set(it.arguments[0] as Boolean)
        }
    }
}

/**
 * Simple shorthand to wait for the fields on a view to be enabled
 */
fun UstadEditView<*>.verifyFieldsEnabled(
    timeoutMillis: Long = 1000
){
    verify(this, timeout(timeoutMillis).atLeastOnce()).fieldsEnabled = true
}
