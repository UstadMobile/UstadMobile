package com.ustadmobile.lib.database.annotation

annotation class UmSyncType {
    companion object {

        const val SYNC_NONE = 0

        const val SYNC_PROACTIVE = 1

        const val SYNC_CACHE = 2
    }

}
