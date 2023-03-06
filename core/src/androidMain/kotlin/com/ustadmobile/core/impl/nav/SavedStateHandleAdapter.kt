package com.ustadmobile.core.impl.nav

import androidx.lifecycle.SavedStateHandle
import com.ustadmobile.door.lifecycle.MutableLiveData

class SavedStateHandleAdapter(val savedStateHandle: SavedStateHandle): UstadSavedStateHandle {

    override fun set(key: String, value: String?) {
        savedStateHandle.set(key, value)
    }

    override fun get(key: String): String? = savedStateHandle.get(key)

    override val keys: Set<String>
        get() = savedStateHandle.keys()

    override fun <T> getLiveData(key: String): MutableLiveData<T> {
        return savedStateHandle.getLiveData(key)
    }
}