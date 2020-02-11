package com.ustadmobile.door.ext

import com.ustadmobile.door.DoorDatabase

actual fun DoorDatabase.dbType() = 0

actual fun DoorDatabase.dbSchemaVersion() = this.dbVersion
