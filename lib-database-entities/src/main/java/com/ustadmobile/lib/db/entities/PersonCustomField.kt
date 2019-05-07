package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
class PersonCustomField {

    @UmPrimaryKey(autoIncrement = true)
    var personCustomFieldUid: Long = 0

    var fieldName: String? = null

    var labelMessageCode: Int = 0

    var fieldIcon: String? = null
}
