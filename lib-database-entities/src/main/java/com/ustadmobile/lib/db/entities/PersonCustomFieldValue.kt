package com.ustadmobile.lib.db.entities


import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
class PersonCustomFieldValue {

    @UmPrimaryKey(autoIncrement = true)
    var personCustomFieldValueUid: Long = 0

    var personCustomFieldValuePersonCustomFieldUid: Long = 0

    var personCustomFieldValuePersonUid: Long = 0

    var fieldValue: String? = null
}
