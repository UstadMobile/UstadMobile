package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.XObjectEntity
import kotlin.js.JsName

@Dao
@UmRepository
abstract class XObjectDao : BaseDao<XObjectEntity> {

    @JsName("findByObjectId")
    @Query("SELECT * from XObjectEntity WHERE objectId = :id")
    abstract fun findByObjectId(id: String?): XObjectEntity?

    @JsName("findByXobjectUid")
    @Query("SELECT * from XObjectEntity WHERE xObjectUid = :xObjectUid")
    abstract fun findByXobjectUid(xObjectUid: Long): XObjectEntity?

}
