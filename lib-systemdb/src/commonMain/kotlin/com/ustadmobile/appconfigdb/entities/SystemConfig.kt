package com.ustadmobile.appconfigdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @param scPasskeyRpId
 *
 * @param scSystemBaseUrl The primary URL of the server : this will be used for
 *   Asset urls for web clients
 *   Client apps will use it as the repository url for lib-systemdb
 *
 * @param scNewPersonalAccountLsUrl the learning space that should be used for creation of personal
 * accounts
 *
 * @param scPresetLsUrl if the system
 */
@Entity
data class SystemConfig(
    @PrimaryKey
    var scUid: Long = SC_UID,

    //e.g. example.org
    var scPasskeyRpId: String = "",

    //This should match the scPasskeyRpId e.g. https://example.org/
    var scSystemBaseUrl: String = "",

    var scNewPersonalAccountLsUrl: String? = null,

    var scPresetLsUrl: String? = null,

    var scLastModified: Long = 0,

    var scAdditionalConfig: String = "{}",

) {

    companion object {

        const val SC_UID = 1L

    }
}
