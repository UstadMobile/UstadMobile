package com.ustadmobile.core.test.viewmodeltest

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.di.CommonJvmDiModule
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.ext.isLazyInitialized
import com.ustadmobile.core.viewmodel.ViewModel
import com.ustadmobile.util.test.nav.TestUstadNavController
import com.ustadmobile.util.test.nav.TestUstadSavedStateHandle
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockWebServer
import org.kodein.di.*
import org.mockito.kotlin.spy
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.nio.file.Files

class ViewModelTestBuilder<T: ViewModel>(
    makeViewModel: ViewModelFactoryParams<T>.() -> T,
    diExtra: (DI.MainBuilder.() -> Unit)?,
) {

    val savedStateHandle = TestUstadSavedStateHandle()

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

        bind<UstadNavController>() with singleton {
            spy(TestUstadNavController())
        }
    }

    val navController: UstadNavController by di.instance()

    init {
        if(diExtra != null) {
            extendDi {
                diExtra()
            }
        }
    }

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
        makeViewModel(ViewModelFactoryParams(di, savedStateHandle, di.direct.instance(),
            this))
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