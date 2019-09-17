package db2

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class ExampleLinkEntity(@PrimaryKey var eeUid: Long = 0L, var fkValue: Long = 0L)
