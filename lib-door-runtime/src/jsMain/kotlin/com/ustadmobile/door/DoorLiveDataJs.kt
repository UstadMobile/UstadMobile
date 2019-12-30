package com.ustadmobile.door

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DoorLiveDataJs<T>(val fetchFn: suspend () -> T) : DoorLiveData<T>() {

    override fun onActive() {
        GlobalScope.launch {
            postValue(fetchFn())
        }
    }
}