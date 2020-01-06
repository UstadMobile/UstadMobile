package com.ustadmobile.door

import androidx.lifecycle.LiveData

actual open class DoorMutableLiveData<T> : LiveData<T> {

    actual constructor(value: T): super(value)

    actual constructor() : super()

    /**
     * Synonymous with postValue. Unfortunately we can't use a straight typeAlias because the
     * MutableLiveData class is overriding a protected method and making it public in Java. Because
     * the protected keyword in Kotlin has a different meaning to the keyword in Java, the compiler
     * will reject the function signatures as being incompatible.
     */
    actual open fun sendValue(value: T) = postValue(value)

    actual open fun setVal(value: T) = setValue(value)


}
