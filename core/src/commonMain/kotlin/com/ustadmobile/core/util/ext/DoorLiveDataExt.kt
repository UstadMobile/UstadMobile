package com.ustadmobile.core.util.ext

import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.door.DoorLiveData

expect fun <T: Any?> DoorLiveData<T>.observeWithPresenter(presenter: UstadBaseController<*>, observer: (T?) -> Unit)
