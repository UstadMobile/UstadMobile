package com.ustadmobile.lib.db.entities

import androidx.room.Entity

@Entity(
    primaryKeys = arrayOf("xolmeXObjectUid", "xolmeHash")
)
/**
 * @param xolmeXObjectUid the XObject uid this is related to
 * @param
 */
data class XObjectLangMapEntry(
    var xolmeXObjectUid: Long = 0,
    var xolmeHash: Long = 0,
    var xolmeLangCode: String? = null,
    var xolmeEntry: String? = null,
) {

    companion object {



    }

}

