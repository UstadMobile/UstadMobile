package com.ustadmobile.lib.db.entities.xapi

import androidx.room.Entity

@Entity(
    primaryKeys = arrayOf("almeActivityUid", "almeHash")
)
/**
 * @param almeActivityUid the activity uid that this lang map is related to
 * @param almeHash hash of "propertyname-langcode" where  propertyname is PROPNAME_NAME or
 * PROPNAME_DESCRIPTION as per XapiActivity and langcode is almeLangCode
 */
data class ActivityLangMapEntry(
    var almeActivityUid: Long = 0,
    var almeHash: Long = 0,
    var almeLangCode: String? = null,
    var almeEntry: String? = null,
    var almeLastMod: Long = 0,
) {


}

