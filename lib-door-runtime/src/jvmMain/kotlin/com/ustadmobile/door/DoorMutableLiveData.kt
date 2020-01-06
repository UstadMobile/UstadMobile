package com.ustadmobile.door

actual open class DoorMutableLiveData<T> : DoorLiveData<T> {

    actual constructor(value: T): super(value) {

    }

    actual constructor()

    actual open fun sendValue(value: T) = postValue(value)

    actual open fun setVal(value: T) = postValue(value)


}