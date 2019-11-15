package com.ustadmobile.core.controller

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver

actual fun <T> DoorLiveData<T>.observe(presenter: UstadBaseController<*>, observer: (T?) -> Unit) {
    val mObserver: dynamic = presenter.context
    this.observe(mObserver, object : DoorObserver<T?> {
        override fun onChanged(t: T?) {
            observer.invoke(t)
        }
    })
}