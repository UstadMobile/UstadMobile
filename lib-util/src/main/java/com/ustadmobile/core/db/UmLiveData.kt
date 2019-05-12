package com.ustadmobile.core.db

import com.ustadmobile.core.impl.UmLifecycleOwner

/**
 * Created by mike on 1/13/18.
 */

interface UmLiveData<T> {

    val value: T

    fun observe(controller: UmLifecycleOwner, observer: UmObserver<T>)

    fun observeForever(observer: UmObserver<T>)

    fun removeObserver(observer: UmObserver<T>)

}
