package com.ustadmobile.appconfigdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @param scPasskeyRpId
 *
 * @param scPrimaryUrl The primary URL of the server : this will be used for
 *   Asset urls for web clients
 *   Android and desktop apps will download this config (as a JSON), which will then determine
 *   certain app behaviors
 *      Whether or not to allow users to create a new account
 *      Whether or not to show users the option to create a personal account
 *      Whether users can select specific learning spaces or always use a preset learning space.
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
    var scPrimaryUrl: String = "",

    var scNewPersonalAccountLsUrl: String? = null,

    var scPresetLsUrl: String? = null,

    var scLastModified: Long = 0,

    var scAdditionalConfig: String = "{}",

) {

    companion object {

        const val SC_UID = 1L

    }
}
