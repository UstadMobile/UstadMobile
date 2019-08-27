package com.ustadmobile.door

actual abstract class DoorMigration {

    actual val startVersion: Int
        get() = TODO("not implemented on Javascript")
    actual val endVersion: Int
        get() = TODO("not implemented on Javascript")

    actual constructor(startVersion: Int, endVersion: Int) {
        TODO("not implemented on Javascript")
    }

    actual abstract fun migrate(database: DoorSqlDatabase)

}