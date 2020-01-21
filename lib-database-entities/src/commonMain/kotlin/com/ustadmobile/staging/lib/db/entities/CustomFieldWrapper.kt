package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

/**
 * POJO Representation of a custom field. This is populated from both PersonField as well as
 * ClazzCustomField
 */
@Serializable
class CustomFieldWrapper {

    var fieldName: String? = null
    var fieldType: String? = null
    var defaultValue: String? = null

    companion object {
        val FIELD_TYPE_DROPDOWN = 2
        val FIELD_TYPE_TEXT = 1
    }
}
