package com.ustadmobile.core.domain.report.formatter

import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_FEMALE
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_MALE

/**
 * Formatter for gender values (Male/Female/Other)
 */
class GenderGraphFormatter : GraphFormatter<String> {
    override fun adjust(value: String): String = value

    override fun format(value: String): String {
        return when (value) {
            GENDER_FEMALE.toString() -> "Female" //TODO Change string value
            GENDER_MALE.toString() -> "Male"
            else -> "Other"
        }
    }
}