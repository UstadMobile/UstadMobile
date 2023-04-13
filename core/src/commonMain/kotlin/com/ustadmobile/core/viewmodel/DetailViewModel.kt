package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import org.kodein.di.DI

abstract class DetailViewModel<T>(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String,
) : UstadViewModel(di, savedStateHandle, destinationName){

    //abstract val entity: Flow<T?>


}