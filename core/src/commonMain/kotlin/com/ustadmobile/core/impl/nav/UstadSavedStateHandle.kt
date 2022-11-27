package com.ustadmobile.core.impl.nav

import com.ustadmobile.door.lifecycle.MutableLiveData

interface UstadSavedStateHandle {

    operator fun <T> set(key: String, value: T?)

    operator fun <T> get(key: String): T?

    fun <T> getLiveData(key:String): MutableLiveData<T>


}