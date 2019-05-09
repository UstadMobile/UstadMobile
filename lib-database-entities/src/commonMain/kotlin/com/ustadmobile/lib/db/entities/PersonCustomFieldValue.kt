package com.ustadmobile.lib.db.entities


import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
@Entity
class PersonCustomFieldValue {

    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    var personCustomFieldValueUid: Long = 0

    var personCustomFieldValuePersonCustomFieldUid: Long = 0

    var personCustomFieldValuePersonUid: Long = 0

    var fieldValue: String? = null
}
