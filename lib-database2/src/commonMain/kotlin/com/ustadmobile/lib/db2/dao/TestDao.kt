package com.ustadmobile.lib.db2.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.lib.db2.TestEntity

@Dao
abstract class TestDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insert(entity: TestEntity)

    @Query("SELECT * FROM TestEntity WHERE uid = :pk")
    abstract fun findByPk(pk: Int): TestEntity

}