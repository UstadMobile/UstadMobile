package com.ustadmobile.core.util.ext

import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.core.controller.UstadBaseController

actual fun <T> DoorLiveData<T>.observeWithPresenter(presenter: UstadBaseController<*>, observer: (T?) -> Unit) {
    this.observe(presenter.context as DoorLifecycleOwner, object : DoorObserver<T?> {
        override fun onChanged(t: T?) {
            observer.invoke(t)
        }
    })
}
