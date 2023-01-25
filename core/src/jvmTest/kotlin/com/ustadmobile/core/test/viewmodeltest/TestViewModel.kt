package com.ustadmobile.core.test.viewmodeltest

import com.ustadmobile.core.viewmodel.ViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

@ViewModelDslMarker
fun <T: ViewModel> testViewModel(
    timeOut: Long = 5000,
    block: suspend ViewModelTestBuilder<T>.() -> Unit
) {
    val viewModelTestBuilder = ViewModelTestBuilder<T>()
    try {
        runBlocking {
            withTimeout(timeOut) {
                block(viewModelTestBuilder)
            }
        }
    }finally {
        viewModelTestBuilder.cleanup()
    }
}