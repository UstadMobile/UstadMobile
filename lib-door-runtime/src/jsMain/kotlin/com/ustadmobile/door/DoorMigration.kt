package com.ustadmobile.door

actual abstract class DoorMigration {

    actual val startVersion: Int
        get() = -1
    actual val endVersion: Int
        get() = -1

    actual constructor(startVersion: Int, endVersion: Int) {
        //do nothing on javascript
    }

    actual abstract fun migrate(database: DoorSqlDatabase)

}