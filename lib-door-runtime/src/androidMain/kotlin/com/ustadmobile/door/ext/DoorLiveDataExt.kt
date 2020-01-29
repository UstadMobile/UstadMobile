package com.ustadmobile.door.ext

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.RepositoryLoadHelper

@Deprecated("We are no longer using the LiveData wrapper so this technique won't work")
fun DoorLiveData<*>.isRepositoryLiveData() = false