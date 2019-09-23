package com.ustadmobile.core.controller

import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver

actual fun <T: Any?> DoorLiveData<T>.observe(presenter: UstadBaseController<*>, observer: (T?) -> Unit) {
    this.observe(presenter.context as DoorLifecycleOwner, object : DoorObserver<T?> {
        override fun onChanged(t: T?) {
            observer.invoke(t)
        }
    })
}
