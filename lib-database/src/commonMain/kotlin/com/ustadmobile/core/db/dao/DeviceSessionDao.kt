package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.lib.db.entities.DeviceSession

@Dao
abstract class DeviceSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(session: DeviceSession)

}