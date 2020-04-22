package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class PresenterFieldQueryRow(
        @Embedded
        var presenterField: PersonDetailPresenterField? = null,

        @Embedded
        var customField: CustomField? = null,

        @Embedded
        var customFieldValue: CustomFieldValue? = null,

        @Embedded
        var customFieldValueOption: CustomFieldValueOption? = null)