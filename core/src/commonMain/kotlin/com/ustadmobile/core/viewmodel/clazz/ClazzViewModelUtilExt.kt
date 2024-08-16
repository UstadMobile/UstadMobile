package com.ustadmobile.core.viewmodel.clazz

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.locale.CourseTerminologyStrings
import com.ustadmobile.lib.db.entities.CourseTerminology
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

/**
 * Function used by various clazz related tabs
 */
suspend fun collectClazzNameAndUpdateTitle(
    clazzUid: Long,
    activeDb: UmAppDatabase,
    appUiState: MutableStateFlow<AppUiState>,
) {
    activeDb.clazzDao().getTitleByUidAsFlow(clazzUid).collect { clazzTitle ->
        appUiState.takeIf { appUiState.value.title != clazzTitle }?.update { prev ->
            prev.copy(title = clazzTitle)
        }
    }
}


/**
 * Function shared by a couple course related view models. Automatically
 * avoids reparsing and updating the terminology if it has not really changed.
 */
fun parseAndUpdateTerminologyStringsIfNeeded(
    currentTerminologyStrings: CourseTerminologyStrings?,
    terminology: CourseTerminology?,
    systemImpl: UstadMobileSystemImpl,
    json: Json,
    onUpdate: (CourseTerminologyStrings?) -> Unit,
) {
    if(currentTerminologyStrings?.terminologyKey != terminology?.ctLct) {
        val courseTerminologyStrings = terminology?.let {
            CourseTerminologyStrings(it, systemImpl, json)
        }
        onUpdate(courseTerminologyStrings)
    }
}
