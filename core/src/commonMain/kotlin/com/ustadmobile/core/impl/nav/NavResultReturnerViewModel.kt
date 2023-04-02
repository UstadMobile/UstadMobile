package com.ustadmobile.core.impl.nav

import com.ustadmobile.core.viewmodel.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter

/**
 * Implementation of NavResultReturner as a ViewModel. It can therefor be used via navGraphViewModels
 * on Android
 */
class NavResultReturnerViewModel(
    savedStateHandle: UstadSavedStateHandle
): ViewModel(savedStateHandle), NavResultReturner {

    private val _resultFlow = MutableSharedFlow<NavResult>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun resultFlowForKey(key: String): Flow<NavResult> {
        return _resultFlow.filter { it.key == key }
    }

    override fun sendResult(result: NavResult) {
        _resultFlow.tryEmit(result)
    }

}