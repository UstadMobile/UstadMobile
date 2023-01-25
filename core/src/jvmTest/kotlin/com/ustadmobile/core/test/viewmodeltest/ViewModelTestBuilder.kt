package com.ustadmobile.core.test.viewmodeltest

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.di.CommonJvmDiModule
import com.ustadmobile.core.impl.nav.NavCommand
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.ext.isLazyInitialized
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.ViewModel
import com.ustadmobile.util.test.nav.TestUstadNavController
import com.ustadmobile.util.test.nav.TestUstadSavedStateHandle
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockWebServer
import org.kodein.di.*
import org.mockito.kotlin.spy
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.nio.file.Files

typealias TestViewModelFactory<T> = ViewModelTestBuilder<T>.() -> T

class ViewModelTestBuilder<T: ViewModel> {

    val savedStateHandle = TestUstadSavedStateHandle()

    private lateinit var viewModelFactory: TestViewModelFactory<T>

    private val xppFactory: XmlPullParserFactory by lazy {
        XmlPullParserFactory.newInstance().also {
            it.isNamespaceAware = true
        }
    }

    /**
     * Temporary directory that can be used by a test. It will be deleted when the test is finished
     */
    val tempDir: File by lazy {
        Files.createTempDirectory("viewmodeltest").toFile()
    }

    /**
     * MockWebServer that can be used by test. If accessed, it will be shutdown
     */
    val mockWebServer: MockWebServer by lazy {
        MockWebServer()
    }

    private var diVar = DI {
        import(CommonJvmDiModule)

        bind<Json>() with singleton {
            Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }
        }

        bind<UstadMobileSystemImpl>() with singleton {
            spy(UstadMobileSystemImpl(xppFactory, tempDir))
        }
    }

    init {

    }

    @ViewModelDslMarker
    fun extendDi(
        block: DI.MainBuilder.() -> Unit,
    ) {
        val extendedDi = DI {
            extend(diVar)
            block()
        }
        diVar = extendedDi
    }

    val di: DI
        get() = diVar

    val viewModel: T by lazy {
        viewModelFactory()
    }

    @ViewModelDslMarker
    fun viewModelFactory(
        block: TestViewModelFactory<T>
    ) {
        viewModelFactory = block
    }

    suspend fun <T> stateInViewModelScope(flow: Flow<T>): StateFlow<T> {
        return flow.stateIn(viewModel.viewModelScope)
    }

    internal fun cleanup() {
        viewModel.viewModelScope.cancel()
        if(this::tempDir.isLazyInitialized) {
            tempDir.deleteRecursively()
            tempDir.deleteOnExit()
        }

        if(this::mockWebServer.isLazyInitialized) {
            mockWebServer.shutdown()
        }

    }

}