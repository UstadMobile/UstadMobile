package com.ustadmobile.core.test.viewmodeltest

import com.ustadmobile.core.viewmodel.ViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.kodein.di.DI

fun <T: ViewModel> testViewModel(
    makeViewModel: ViewModelFactoryParams<T>.() -> T,
    extendDi: (DI.MainBuilder.() -> Unit)? = null,
    timeOut: Long = 5000,
    block: suspend ViewModelTestBuilder<T>.() -> Unit
) {
    val viewModelTestBuilder = ViewModelTestBuilder(makeViewModel, extendDi)
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