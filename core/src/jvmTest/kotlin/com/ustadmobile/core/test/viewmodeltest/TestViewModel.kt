package com.ustadmobile.core.test.viewmodeltest

import com.ustadmobile.core.viewmodel.ViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

data class TestRepoConfig(
    val useDbAsRepo: Boolean = true
)

@ViewModelDslMarker
fun <T: ViewModel> testViewModel(
    timeOut: Long = 5000,
    repoConfig: TestRepoConfig = TestRepoConfig(),
    block: suspend ViewModelTestBuilder<T>.() -> Unit
) {
    val viewModelTestBuilder = ViewModelTestBuilder<T>(repoConfig)
    try {
        runBlocking {
            withTimeout(timeOut) {
                block(viewModelTestBuilder)
            }
        }
    }catch(e: Exception) {
        Napier.e("Exception running test: $e", e)
        throw e
    }finally {
        viewModelTestBuilder.cleanup()
    }
}