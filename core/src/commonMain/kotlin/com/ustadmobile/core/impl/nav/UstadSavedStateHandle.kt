package com.ustadmobile.core.impl.nav

import com.ustadmobile.door.lifecycle.MutableLiveData

interface UstadSavedStateHandle {

    operator fun set(key: String, value: String?)

    operator fun get(key: String): String?

    val keys: Set<String>

    @Deprecated("This should NOT be used with MVVM. Data should be returned via bus")
    fun <T> getLiveData(key:String): MutableLiveData<T>


}