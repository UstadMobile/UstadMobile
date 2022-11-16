package com.ustadmobile.core.test.viewmodeltest

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.di.CommonJvmDiModule
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.viewmodel.ViewModel
import com.ustadmobile.util.test.nav.TestUstadNavController
import com.ustadmobile.util.test.nav.TestUstadSavedStateHandle
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.mockito.kotlin.spy
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.nio.file.Files

class ViewModelTestBuilder<T: ViewModel>(
    makeViewModel: ViewModelFactoryParams.() -> T
) {

    val savedStateHandle = TestUstadSavedStateHandle()

    private val xppFactory: XmlPullParserFactory by lazy {
        XmlPullParserFactory.newInstance().also {
            it.isNamespaceAware = true
        }
    }

    private val tempDir: File by lazy {
        Files.createTempDirectory("viewmodeltest").toFile()
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
        makeViewModel(ViewModelFactoryParams(di, savedStateHandle))
    }

}