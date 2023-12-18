package com.ustadmobile.core.test.viewmodeltest


import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import moe.tlaster.precompose.viewmodel.ViewModel

data class TestRepoConfig(
    val useDbAsRepo: Boolean = true
)

@ViewModelDslMarker
fun <T: ViewModel> testViewModel(
    repoConfig: TestRepoConfig = TestRepoConfig(),
    block: suspend ViewModelTestBuilder<T>.() -> Unit
) {
    val viewModelTestBuilder = ViewModelTestBuilder<T>(repoConfig)
    try {
        runBlocking {
            block(viewModelTestBuilder)
        }
    }catch(e: Exception) {
        Napier.e("Exception running test: $e", e)
        throw e
    }finally {
        viewModelTestBuilder.cleanup()
    }
}