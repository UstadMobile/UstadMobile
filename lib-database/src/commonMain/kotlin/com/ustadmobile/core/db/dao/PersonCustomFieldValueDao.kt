package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue

@UmDao(inheritPermissionFrom = PersonDao::class, inheritPermissionForeignKey = "personCustomFieldValuePersonUid", inheritPermissionJoinedPrimaryKey = "personUid")
@Dao
abstract class PersonCustomFieldValueDao : BaseDao<PersonCustomFieldValue> {

    @Insert
    abstract override fun insert(entity: PersonCustomFieldValue): Long

    @Insert
    abstract override suspend fun insertAsync(entity: PersonCustomFieldValue): Long

    @Query("SELECT * FROM PersonCustomFieldValue WHERE personCustomFieldValueUid = :uid")
    abstract fun findByUid(uid: Long): PersonCustomFieldValue?

    @Query("SELECT * FROM PersonCustomFieldValue WHERE personCustomFieldValuePersonUid = :personUid")
    abstract suspend fun findByPersonUidAsync(personUid: Long): List<PersonCustomFieldValue>

}
