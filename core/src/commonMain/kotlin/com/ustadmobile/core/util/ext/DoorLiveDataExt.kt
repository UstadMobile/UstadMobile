package com.ustadmobile.core.util.ext

import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.lifecycle.LiveData

expect fun <T: Any?> LiveData<T>.observeWithLifecycleOwner(
    lifecycleOwner: LifecycleOwner,
    observer: (T?) -> Unit
)
