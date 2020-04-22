package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class PresenterFieldRow(

    var presenterField: PersonDetailPresenterField? = null,

    var customField: CustomField? = null,

    var customFieldValue: CustomFieldValue? = null,

    var customFieldOptions: List<CustomFieldValueOption> = listOf()
)