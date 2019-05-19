package com.ustadmobile.core.db.dao

import androidx.room.Dao
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.PersonLocationJoin

@UmDao(inheritPermissionFrom = PersonDao::class, inheritPermissionForeignKey = "personLocationPersonUid", inheritPermissionJoinedPrimaryKey = "personUid")
@Dao
@UmRepository
abstract class PersonLocationJoinDao : SyncableDao<PersonLocationJoin, PersonLocationJoinDao>
