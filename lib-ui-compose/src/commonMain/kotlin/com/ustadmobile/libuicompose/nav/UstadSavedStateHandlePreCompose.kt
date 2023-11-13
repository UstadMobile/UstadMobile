package com.ustadmobile.libuicompose.nav

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.libuicompose.util.ext.urlDecode
import moe.tlaster.precompose.navigation.BackStackEntry


class UstadSavedStateHandlePreCompose(
    private val backStackEntry: BackStackEntry
) : UstadSavedStateHandle{

    override fun set(key: String, value: String?) {
        backStackEntry.savedStateHolder.registerProvider(key) { value }
    }

    override fun get(key: String): String? {
        return backStackEntry.savedStateHolder.consumeRestored(key)?.toString()
            ?: backStackEntry.queryString?.map?.get(key)?.firstOrNull()?.urlDecode()
    }

    override val keys: Set<String>
        get() = throw IllegalStateException("Not supported on precompose")


    @Deprecated("")
    override fun <T> getLiveData(key: String): MutableLiveData<T> {
        TODO("Not yet implemented")
    }
}