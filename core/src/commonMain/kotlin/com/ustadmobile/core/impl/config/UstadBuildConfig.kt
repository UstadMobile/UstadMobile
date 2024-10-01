package com.ustadmobile.core.impl.config

/**
 * Used to set build time configuration options including:
 *
 * com.ustadmobile.uilanguages - UI language options to show
 * com.ustadmobile.presetlocale - default locale to use at startup
 *
 * On Android: properties must be set in AndroidManifest.xml as meta-data (see examples in file).
 *  This is then picked via Android's API to read manifest metadata.
 * On Web: properties are set on a BuildConfig object - see app-react/build.gradle
 * On JVM: properties should be set in the JAR manifest
 */
interface UstadBuildConfig {

    operator fun get(key: String): String?

    companion object {

        const val KEY_GENDER_CONFIG = "com.ustadmobile.gopts"

        const val KEY_CONFIG_SHOW_POWERED_BY = "com.ustadmobile.showpoweredbymsg"

        const val KEY_SYSTEM_URL = "com.ustadmobile.system.systemBaseUrl"

        const val KEY_PRESET_LEARNING_SPACE_URL = "com.ustadmobile.system.presetLsUrl"

        const val KEY_PASSKEY_RP_ID = "com.ustadmobile.system.passkeyRpId"

        const val KEY_NEW_PERSONAL_ACCOUNT_LEARNING_SPACE_URL = "com.ustadmobile.system.newPersonalAccountsLsUrl"

    }

}