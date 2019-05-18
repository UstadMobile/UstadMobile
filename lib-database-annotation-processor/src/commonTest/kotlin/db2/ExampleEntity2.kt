package db2

import android.arch.persistence.room.ColumnInfo
import androidx.room.PrimaryKey

data class ExampleEntity2(
        @PrimaryKey(autoGenerate = true)
        var uid: Long,
        var name: String,
        @ColumnInfo(index = true)
        var someNumber: Long)
