package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzMember

@UmDao(inheritPermissionFrom = ClazzDao::class, inheritPermissionForeignKey = "clazzMemberClazzUid", inheritPermissionJoinedPrimaryKey = "clazzUid")
@Dao
@UmRepository
abstract class ClazzMemberDao : BaseDao<ClazzMember> {

    @Insert
    abstract override fun insert(entity: ClazzMember): Long

    @Insert
    abstract override suspend fun insertAsync(entity: ClazzMember): Long

    @Query("SELECT * FROM ClazzMember WHERE clazzMemberUid = :uid")
    abstract fun findByUid(uid: Long): ClazzMember?
}
