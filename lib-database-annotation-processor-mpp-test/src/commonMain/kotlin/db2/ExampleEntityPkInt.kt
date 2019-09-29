package db2

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class ExampleEntityPkInt(
        @PrimaryKey(autoGenerate = true)  var pk: Int = 0,
        var str: String? = null)