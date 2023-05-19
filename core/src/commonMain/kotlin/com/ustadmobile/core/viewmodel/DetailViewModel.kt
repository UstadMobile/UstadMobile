package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import org.kodein.di.DI

abstract class DetailViewModel<T>(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String,
) : UstadViewModel(di, savedStateHandle, destinationName){

    //abstract val entity: Flow<T?>

    protected val entityUidArg: Long by lazy(LazyThreadSafetyMode.NONE) {
        savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0
    }

}