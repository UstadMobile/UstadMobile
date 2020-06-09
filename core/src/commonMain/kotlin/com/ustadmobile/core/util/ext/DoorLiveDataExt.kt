package com.ustadmobile.core.util.ext

import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData

@Deprecated("DO NOT use this method with views implementing using fragments! On Android " +
        "this will assume that the presenter context is the LifecycleOwner, which is not the case " +
        "with views implemented using a fragment. This can lead to memory leaks.")
expect fun <T: Any?> DoorLiveData<T>.observeWithPresenter(presenter: UstadBaseController<*>, observer: (T?) -> Unit)

expect fun <T: Any?> DoorLiveData<T>.observeWithLifecycleOwner(lifecycleOwner: DoorLifecycleOwner, observer: (T?) -> Unit)
