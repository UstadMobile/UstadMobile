package com.ustadmobile.util.test.nav

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.DoorMutableLiveData
import java.util.concurrent.ConcurrentHashMap

/**
 * Basic test implementation of SavedStateHandle. Provides no persistence support
 */
class TestUstadSavedStateHandle: UstadSavedStateHandle {

    private val mLiveDatas: MutableMap<String, DoorMutableLiveData<*>> = ConcurrentHashMap()

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