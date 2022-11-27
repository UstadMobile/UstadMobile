package com.ustadmobile.util.test.nav

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.lifecycle.MutableLiveData
import java.util.concurrent.ConcurrentHashMap

/**
 * Basic test implementation of SavedStateHandle. Provides no persistence support
 */
class TestUstadSavedStateHandle: UstadSavedStateHandle {

    private val mLiveDatas: MutableMap<String, MutableLiveData<*>> = ConcurrentHashMap()

    override fun <T> set(key: String, value: T?) {
        mLiveDatas[key] = MutableLiveData(value as Any)
    }

    override fun <T> get(key: String): T? {
        return mLiveDatas[key]?.getValue() as T?
    }

    override fun <T> getLiveData(key: String): MutableLiveData<T> {
        return mLiveDatas.getOrPut(key) {
            MutableLiveData(null)
        } as MutableLiveData<T>
    }
}