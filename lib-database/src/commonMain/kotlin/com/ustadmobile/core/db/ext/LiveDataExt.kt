package com.ustadmobile.core.db.ext

import com.ustadmobile.door.lifecycle.LiveData

expect suspend  fun <T> LiveData<T>.getFirstValue(): T
