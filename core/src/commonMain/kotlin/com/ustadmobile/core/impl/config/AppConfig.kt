package com.ustadmobile.core.impl.config

/**
 * Used to set build time configuration options including:
 *
 * com.ustadmobile.uilanguages - UI language options to show
 * com.ustadmobile.presetlocale - default locale to use at startup
 *
 * On Android: properties must be set in AndroidManifest.xml as meta-data (see examples in file).
 *  This is then picked via Android's API to read manifest metadata.
 * On Web: properties must be set in app-react/src/jsMain/resources/ustad-config.json . The JSON
 *  file will be read on app load.
 * On JVM: properties should be set in the JAR manifest
 */
interface AppConfig {

    operator fun get(key: String): String?

    companion object {

        const val KEY_GENDER_CONFIG = "com.ustadmobile.gopts"

        const val KEY_CONFIG_SHOW_POWERED_BY = "com.ustadmobile.showpoweredbymsg"

        const val KEY_API_URL = "com.ustadmobile.apiurl"

    }

}