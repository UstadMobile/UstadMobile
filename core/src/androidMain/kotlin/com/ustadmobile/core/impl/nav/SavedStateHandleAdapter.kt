package com.ustadmobile.core.impl.nav

import androidx.lifecycle.SavedStateHandle
import com.ustadmobile.door.lifecycle.MutableLiveData

class SavedStateHandleAdapter(val savedStateHandle: SavedStateHandle): UstadSavedStateHandle {

    override fun <T> set(key: String, value: T?) {
        savedStateHandle.set(key, value)
    }

    override fun <T> get(key: String): T? = savedStateHandle.get(key)

    override fun <T> getLiveData(key: String): MutableLiveData<T> {
        return savedStateHandle.getLiveData(key)
    }
}