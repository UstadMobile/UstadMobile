package com.ustadmobile.core.viewmodel.clazz

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.appstate.AppUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Function used by various clazz related tabs
 */
suspend fun collectClazzNameAndUpdateTitle(
    clazzUid: Long,
    activeDb: UmAppDatabase,
    appUiState: MutableStateFlow<AppUiState>,
) {
    activeDb.clazzDao.getTitleByUidAsFlow(clazzUid).collect { clazzTitle ->
        appUiState.takeIf { appUiState.value.title != clazzTitle }?.update { prev ->
            prev.copy(title = clazzTitle)
        }
    }
}
