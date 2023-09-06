package com.ustadmobile.lib.rest

import com.ustadmobile.door.DoorDatabaseCallbackSync
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.DoorSqlDatabase
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.util.randomString

class InsertDefaultSiteCallback: DoorDatabaseCallbackSync {
    override fun onCreate(db: DoorSqlDatabase) {
        val falseStr = if(db.dbType() == DoorDbType.SQLITE) "0" else "false"
        val createSiteSql = """INSERT INTO Site(siteUid, sitePcsn, siteLcsn, siteLcb, siteLct, siteName, guestLogin, registrationAllowed, authSalt) 
                      VALUES(1, 0, 0, 0, ${systemTimeInMillis()}, 'My site', $falseStr, $falseStr, '${randomString(20)}')
            """.trimMargin()
        db.execSQL(createSiteSql)
    }

    override fun onOpen(db: DoorSqlDatabase) {
        //Do nothing
    }
}