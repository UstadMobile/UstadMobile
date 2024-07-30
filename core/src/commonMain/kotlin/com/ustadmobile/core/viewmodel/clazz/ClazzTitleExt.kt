package com.ustadmobile.core.viewmodel.clazz

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.door.ext.DoorTag
import kotlinx.coroutines.launch
import org.kodein.di.instance

fun UstadViewModel.launchSetTitleFromClazzUid(
    clazzUid: Long,
    updateUi: (String?) -> Unit
) {
    val repo: UmAppDatabase by di.onActiveEndpoint().instance(tag = DoorTag.TAG_REPO)

    viewModelScope.takeIf { clazzUid != 0L }?.launch {
        repo.clazzDao().getTitleByUidAsFlow(clazzUid).collect {
            updateUi(it)
        }
    }
}
