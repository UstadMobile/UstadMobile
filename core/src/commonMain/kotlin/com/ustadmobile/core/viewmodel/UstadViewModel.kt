package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import org.kodein.di.DI
import org.kodein.di.DIAware

abstract class UstadViewModel(
    override val di: DI,
    savedStateHandle: UstadSavedStateHandle,
): ViewModel(savedStateHandle), DIAware {


}