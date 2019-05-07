package com.ustadmobile.lib.db.entities

interface SyncableEntity {

    var masterChangeSeqNum: Long

    var localChangeSeqNum: Long

}
