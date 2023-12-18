package com.ustadmobile.core.test.viewmodeltest

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.serialization.json.Json
import moe.tlaster.precompose.viewmodel.ViewModel
import org.kodein.di.DI

class ViewModelFactoryParams<T: ViewModel>(
    val di: DI,
    val savedStateHandle: UstadSavedStateHandle,
    val json: Json,
    val testContext: ViewModelTestBuilder<T>,
)
