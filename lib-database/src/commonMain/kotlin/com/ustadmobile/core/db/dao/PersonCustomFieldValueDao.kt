package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue
import com.ustadmobile.lib.db.entities.PersonCustomFieldWithPersonCustomFieldValue

@UmRepository
@UmDao(inheritPermissionFrom = PersonDao::class, 
        inheritPermissionForeignKey = "personCustomFieldValuePersonUid",
        inheritPermissionJoinedPrimaryKey = "personUid")
@Dao
abstract class PersonCustomFieldValueDao : BaseDao<PersonCustomFieldValue> {

    @Insert
    abstract override fun insert(entity: PersonCustomFieldValue): Long


    @Query("SELECT * FROM PersonCustomFieldValue WHERE personCustomFieldValueUid = :uid")
    abstract fun findByUid(uid: Long): PersonCustomFieldValue?

    @Update
    abstract suspend fun updateAsync(entity: PersonCustomFieldValue) : Int

    @Update
    abstract suspend fun updateListAsync(entities: List<PersonCustomFieldValue>) : Int




}
