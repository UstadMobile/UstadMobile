package com.ustadmobile.navigation

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.DoorMutableLiveData

class UstadSavedStateHandleJs: UstadSavedStateHandle {

    internal val mLiveData: MutableMap<String, DoorMutableLiveData<*>?> = mutableMapOf()

    override fun <T> set(key: String, value: T?) {
        mLiveData[key] = DoorMutableLiveData(value)
    }

    override fun <T> get(key: String): T? {
        return mLiveData[key]?.getValue().unsafeCast<T>()
    }

    override fun <T> getLiveData(key: String): DoorMutableLiveData<T> {
        return mLiveData.getOrPut(key) {
            DoorMutableLiveData(null)
        }.unsafeCast<DoorMutableLiveData<T>>()
    }
}