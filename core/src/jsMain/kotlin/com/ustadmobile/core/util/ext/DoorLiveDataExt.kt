package com.ustadmobile.core.util.ext

import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.lifecycle.Observer

actual fun <T: Any?> LiveData<T>.observeWithLifecycleOwner(lifecycleOwner: LifecycleOwner, observer: (T?) -> Unit) {
    this.observe(lifecycleOwner, object : Observer<T?> {
        override fun onChanged(t: T?) {
            observer.invoke(t)
        }
    })
}
