package com.ustadmobile.core.viewmodel.clazz

import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.launch
import org.kodein.di.instance

fun UstadViewModel.launchSetTitleFromClazzUid(
    clazzUid: Long,
    updateUi: (String?) -> Unit
) {
    val dataLayer: UmAppDataLayer by di.onActiveEndpoint().instance()

    viewModelScope.takeIf { clazzUid != 0L }?.launch {
        dataLayer.repositoryOrLocalDb.clazzDao().getTitleByUidAsFlow(clazzUid).collect {
            updateUi(it)
        }
    }
}
