package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.XObjectEntity
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class XObjectDao : BaseDao<XObjectEntity> {

    @JsName("findByObjectId")
    @Query("SELECT * from XObjectEntity WHERE objectId = :id")
    abstract fun findByObjectId(id: String?): XObjectEntity?

    @JsName("findByXobjectUid")
    @Query("SELECT * from XObjectEntity WHERE xObjectUid = :xObjectUid")
    abstract fun findByXobjectUid(xObjectUid: Long): XObjectEntity?

}
