package com.ustadmobile.core.domain.report.formatter

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.locale.StringResourceUiText
import com.ustadmobile.core.impl.locale.UiText
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_FEMALE
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_MALE

class GenderGraphFormatter : GraphFormatter<String> {

    override fun adjust(value: String): String {
        return value
    }

    override fun format(value: String): UiText {
        return when (value) {
            GENDER_FEMALE.toString() -> StringResourceUiText(MR.strings.female)
            GENDER_MALE.toString() -> StringResourceUiText(MR.strings.male)
            else -> StringResourceUiText(MR.strings.other)
        }
    }
}