package com.ustadmobile.util.test.nav

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.lifecycle.MutableLiveData
import java.util.concurrent.ConcurrentHashMap

/**
 * Basic test implementation of SavedStateHandle. Provides no persistence support
 */
class TestUstadSavedStateHandle: UstadSavedStateHandle {

    private val mLiveDatas: MutableMap<String, MutableLiveData<String?>> = ConcurrentHashMap()

    override fun set(key: String, value: String?) {
        mLiveDatas[key] = MutableLiveData(value)
    }

    override fun get(key: String): String? {
        return mLiveDatas[key]?.getValue()
    }

    override val keys: Set<String>
        get() = mLiveDatas.keys.toSet()

    @Deprecated("This should NOT be used in MVVM")
    override fun <T> getLiveData(key: String): MutableLiveData<T> {
        return mLiveDatas.getOrPut(key) {
            MutableLiveData(null)
        } as MutableLiveData<T>
    }
}