package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
data class PresenterFieldRow(

    var presenterField: PersonDetailPresenterField? = null,

    var customField: CustomField? = null,

    var customFieldValue: CustomFieldValue? = null,

    var customFieldOptions: List<CustomFieldValueOption> = listOf()
)