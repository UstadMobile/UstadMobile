package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.lib.db.entities.NotificationSettingLastChecked

@Dao
abstract class NotificationSettingLastCheckedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceAsync(entity: NotificationSettingLastChecked)

}