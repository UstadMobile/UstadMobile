package com.ustadmobile.core.util.ext

import com.ustadmobile.door.ChangeListenerRequest
import com.ustadmobile.door.DoorDatabase

expect fun DoorDatabase.addInvalidationListener(changeListenerRequest: ChangeListenerRequest)

expect fun DoorDatabase.removeInvalidationListener(changeListenerRequest: ChangeListenerRequest)