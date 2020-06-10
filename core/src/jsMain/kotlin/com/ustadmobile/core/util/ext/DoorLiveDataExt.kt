package com.ustadmobile.core.util.ext

import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver

actual fun <T> DoorLiveData<T>.observeWithPresenter(presenter: UstadBaseController<*>, observer: (T?) -> Unit) {
    val mObserver: dynamic = presenter.context
    this.observe(mObserver, object : DoorObserver<T?> {
        override fun onChanged(t: T?) {
            observer.invoke(t)
        }
    })
}

actual fun <T: Any?> DoorLiveData<T>.observeWithLifecycleOwner(lifecycleOwner: DoorLifecycleOwner, observer: (T?) -> Unit) {
    this.observe(lifecycleOwner, object : DoorObserver<T?> {
        override fun onChanged(t: T?) {
            observer.invoke(t)
        }
    })
}
