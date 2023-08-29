package com.ustadmobile.core.util

import dev.icerock.moko.resources.StringResource

/**
 * There are various options in the app where we have an integer flag of some kind (e.g.
 * SORT_BY_NAME_ASC) and a corresponding MR.strings.that represents this value to the user e.g.
 * MR.strings.sort_by_name_asc . This is often used in dropdowns, menu chips, sort options, etc.
 *
 * This is a basic data class to encapsulate such options.
 */
data class MessageIdOption2(val stringResource: StringResource, val value: Int)
