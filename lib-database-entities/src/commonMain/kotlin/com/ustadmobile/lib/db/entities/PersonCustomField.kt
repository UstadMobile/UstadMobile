package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class PersonCustomField() {

    @PrimaryKey(autoGenerate = true)
    var personCustomFieldUid: Long = 0

    var fieldName: String? = null

    var labelMessageCode: Int = 0

    var fieldIcon: String? = null
}
