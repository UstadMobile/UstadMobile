package com.ustadmobile.core.test.viewmodeltest

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.ViewModel
import kotlinx.serialization.json.Json
import org.kodein.di.DI

class ViewModelFactoryParams<T: ViewModel>(
    val di: DI,
    val savedStateHandle: UstadSavedStateHandle,
    val json: Json,
    val testContext: ViewModelTestBuilder<T>,
)
