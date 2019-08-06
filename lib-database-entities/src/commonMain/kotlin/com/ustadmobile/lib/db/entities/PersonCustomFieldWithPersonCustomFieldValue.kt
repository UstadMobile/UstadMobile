package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEmbedded

/**
 * Represents Field Value pair for fields (custom)
 *
 */
class PersonCustomFieldWithPersonCustomFieldValue : PersonField() {

    @UmEmbedded
    var customFieldValue: PersonCustomFieldValue? = null
}
