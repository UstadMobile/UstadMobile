package db2

import android.arch.persistence.room.ColumnInfo
import androidx.room.PrimaryKey

data class ExampleEntity2(
        @PrimaryKey(autoGenerate = true)
        var uid: Long = 0L,
        var name: String = "",
        @ColumnInfo(index = true)
        var someNumber: Long = 0L)
