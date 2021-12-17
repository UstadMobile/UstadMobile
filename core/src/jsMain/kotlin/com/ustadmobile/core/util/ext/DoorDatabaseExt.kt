package com.ustadmobile.core.util.ext

import com.ustadmobile.door.ChangeListenerRequest
import com.ustadmobile.door.DoorDatabase

actual fun DoorDatabase.addInvalidationListener(changeListenerRequest: ChangeListenerRequest) {
    addChangeListener(changeListenerRequest)
}

actual fun DoorDatabase.removeInvalidationListener(changeListenerRequest: ChangeListenerRequest) {
    removeChangeListener(changeListenerRequest)
}