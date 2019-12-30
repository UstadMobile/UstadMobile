package com.ustadmobile.door

expect open class DoorMutableLiveData<T>: DoorLiveData<T>  {

    constructor(value: T)

    constructor()

    open fun sendValue(value: T)

    open fun setVal(value: T)

}