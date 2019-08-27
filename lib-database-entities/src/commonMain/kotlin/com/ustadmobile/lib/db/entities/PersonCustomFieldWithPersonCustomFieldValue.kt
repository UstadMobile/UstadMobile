package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

/**
 * Represents Field Value pair for fields (custom)
 *
 */
class PersonCustomFieldWithPersonCustomFieldValue : PersonField() {

    @Embedded
    var customFieldValue: PersonCustomFieldValue? = null
}
