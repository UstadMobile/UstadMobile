package com.ustadmobile.door.ext

import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.DoorDbType

actual fun DoorDatabase.dbType(): Int = DoorDbType.SQLITE
