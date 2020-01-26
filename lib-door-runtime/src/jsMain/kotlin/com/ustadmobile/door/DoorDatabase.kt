package com.ustadmobile.door

import kotlinx.coroutines.Runnable

actual abstract class DoorDatabase {

    abstract val dbVersion: Int

    actual abstract fun clearAllTables()

    actual open fun runInTransaction(runnable: Runnable) {
        runnable.run()
    }

}