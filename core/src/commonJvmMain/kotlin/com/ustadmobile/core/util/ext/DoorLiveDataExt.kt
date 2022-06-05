package com.ustadmobile.core.util.ext

import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.core.controller.UstadBaseController

actual fun <T: Any?> DoorLiveData<T>.observeWithLifecycleOwner(
    lifecycleOwner: DoorLifecycleOwner,
    observer: (T?) -> Unit
) {
    this.observe(lifecycleOwner, object : DoorObserver<T?> {
        override fun onChanged(t: T?) {
            observer.invoke(t)
        }
    })
}
