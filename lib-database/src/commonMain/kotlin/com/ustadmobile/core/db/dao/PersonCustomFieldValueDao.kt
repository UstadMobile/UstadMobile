package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue
import com.ustadmobile.lib.db.entities.PersonCustomFieldWithPersonCustomFieldValue

@UmRepository
@UmDao(inheritPermissionFrom = PersonDao::class, 
        inheritPermissionForeignKey = "personCustomFieldValuePersonUid",
        inheritPermissionJoinedPrimaryKey = "personUid")
abstract class PersonCustomFieldValueDao : BaseDao<PersonCustomFieldValue> {

    @Insert
    abstract override fun insert(entity: PersonCustomFieldValue): Long

    @Insert
    abstract fun insertAsync(entity: PersonCustomFieldValue, resultObject: UmCallback<Long>)

    @Query("SELECT * FROM PersonCustomFieldValue WHERE personCustomFieldValueUid = :uid")
    abstract fun findByUid(uid: Long): PersonCustomFieldValue

    @Query("SELECT * FROM PersonCustomFieldValue WHERE " +
            "personCustomFieldValuePersonUid = :personUid AND " +
            "personCustomFieldValuePersonCustomFieldUid = :fieldUid")
    abstract fun findCustomFieldByFieldAndPersonAsync(fieldUid: Long, personUid: Long,
                                                      resultObject: UmCallback<PersonCustomFieldValue>)

    @Update
    abstract fun updateAsync(entity: PersonCustomFieldValue, resultObject: UmCallback<Int>)

    @Update
    abstract fun updateListAsync(entities: List<PersonCustomFieldValue>,
                                 callback: UmCallback<Int>)


    @Query("SELECT * FROM PersonField " +
            "LEFT JOIN PersonCustomFieldValue ON " +
            "PersonCustomFieldValue.personCustomFieldValuePersonCustomFieldUid = " +
            " PersonField.personCustomFieldUid " +
            "WHERE personCustomFieldValuePersonUid = :personUid")
    abstract fun findByPersonUidAsync2(personUid: Long,
                                       callback: UmCallback<List<PersonCustomFieldWithPersonCustomFieldValue>>)


}
