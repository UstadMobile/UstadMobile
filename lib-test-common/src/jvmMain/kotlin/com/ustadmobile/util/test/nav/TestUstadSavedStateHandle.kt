package com.ustadmobile.util.test.nav

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle

/**
 * Basic test implementation of SavedStateHandle. Provides no persistence support
 */
class TestUstadSavedStateHandle: UstadSavedStateHandle {

    private val mSavedData = mutableMapOf<String, String>()

    override fun set(key: String, value: String?) {
        if(value != null) {
            mSavedData[key] = value
        }else {
            mSavedData.remove(key)
        }
    }

    override fun get(key: String): String? {
        return mSavedData[key]
    }

    override val keys: Set<String>
        get() = mSavedData.keys
}