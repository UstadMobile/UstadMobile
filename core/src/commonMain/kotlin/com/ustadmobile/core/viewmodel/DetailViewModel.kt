package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI

abstract class DetailViewModel<T>(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle){

    //abstract val entity: Flow<T?>


}