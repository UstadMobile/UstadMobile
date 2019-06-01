package db2

import androidx.room.PrimaryKey

data class ExampleEntityPkInt(
        @PrimaryKey(autoGenerate = true)  var pk: Int = 0,
        var str: String? = null)