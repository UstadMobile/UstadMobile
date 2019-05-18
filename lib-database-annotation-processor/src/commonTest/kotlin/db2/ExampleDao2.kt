package db2

import androidx.room.Dao
import androidx.room.Insert

@Dao
abstract class ExampleDao2 {

    @Insert
    abstract fun insertList(entityList: List<ExampleEntity2>)

}