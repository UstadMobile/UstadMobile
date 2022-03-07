package com.ustadmobile.core.util.ext

import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData

expect fun <T: Any?> DoorLiveData<T>.observeWithLifecycleOwner(
    lifecycleOwner: DoorLifecycleOwner,
    observer: (T?) -> Unit
)
