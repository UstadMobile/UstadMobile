package com.ustadmobile.core.db.dao

import androidx.room.Insert
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.lib.db.entities.EnrolmentRequest

@DoorDao
expect abstract class EnrolmentRequestDao {

    @Insert
    abstract suspend fun insert(enrolmentRequest: EnrolmentRequest)

}