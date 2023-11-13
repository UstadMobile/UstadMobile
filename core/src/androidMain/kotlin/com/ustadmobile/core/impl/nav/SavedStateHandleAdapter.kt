package com.ustadmobile.core.impl.nav

import androidx.lifecycle.SavedStateHandle

class SavedStateHandleAdapter(val savedStateHandle: SavedStateHandle): UstadSavedStateHandle {

    override fun set(key: String, value: String?) {
        savedStateHandle.set(key, value)
    }

    override fun get(key: String): String? = savedStateHandle.get(key)

    override val keys: Set<String>
        get() = savedStateHandle.keys()

}