package com.ustadmobile.core.controller

import com.ustadmobile.door.DoorLiveData

expect fun <T: Any?> DoorLiveData<T>.observe(presenter: UstadBaseController<*>, observer: (T?) -> Unit)
