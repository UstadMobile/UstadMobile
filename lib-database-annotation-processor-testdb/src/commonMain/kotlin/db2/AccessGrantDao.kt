package db2

import androidx.room.Dao
import androidx.room.Insert

@Dao
abstract class AccessGrantDao {

    @Insert
    abstract fun insert(entity: AccessGrant)

}