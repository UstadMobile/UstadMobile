package db2

import androidx.room.Entity

@Entity
data class ExampleLinkEntity(var eeUid: Long = 0L, var fkValue: Long = 0L)
