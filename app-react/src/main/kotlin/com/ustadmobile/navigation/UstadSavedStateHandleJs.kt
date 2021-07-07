package com.ustadmobile.navigation

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ext.concurrentSafeMapOf

class UstadSavedStateHandleJs: UstadSavedStateHandle {

    private val mLiveDatas: MutableMap<String, DoorMutableLiveData<*>> = concurrentSafeMapOf()

    override fun <T> set(key: String, value: T?) {
        mLiveDatas[key] = DoorMutableLiveData(value as Any)
    }

    override fun <T> get(key: String): T? {
        return mLiveDatas[key]?.getValue() as T?
    }

    override fun <T> getLiveData(key: String): DoorMutableLiveData<T> {
        return mLiveDatas.getOrPut(key) {
            DoorMutableLiveData(null)
        } as DoorMutableLiveData<T>
    }
}