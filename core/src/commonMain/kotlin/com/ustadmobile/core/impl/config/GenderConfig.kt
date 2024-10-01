package com.ustadmobile.core.impl.config

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants.GENDER_MESSAGE_IDS
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.toNullIfBlank
import com.ustadmobile.lib.db.entities.Person

class GenderConfig(
    genderConfigStr: String = DEFAULT_GENDER_OPTIONS,
) {

    constructor(appConfig: UstadBuildConfig): this(
        appConfig[UstadBuildConfig.KEY_GENDER_CONFIG]?.toNullIfBlank() ?: DEFAULT_GENDER_OPTIONS
    )

    private val genderOptions = genderConfigStr.split(",")
        .filter { it.isNotBlank() }.map { it.toInt() }

    private val genderMessageIds = GENDER_MESSAGE_IDS.filter {
        it.value in genderOptions
    }

    val genderMessageIdsAndUnset = genderMessageIds + listOf(MessageIdOption2(MR.strings.blank, Person.GENDER_UNSET))

    companion object {

        const val DEFAULT_GENDER_OPTIONS = "${Person.GENDER_FEMALE},${Person.GENDER_MALE},${Person.GENDER_OTHER}"

    }

}