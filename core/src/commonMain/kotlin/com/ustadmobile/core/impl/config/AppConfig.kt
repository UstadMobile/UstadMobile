package com.ustadmobile.core.impl.config

interface AppConfig {

    operator fun get(key: String): String?

    companion object {

        const val KEY_GENDER_CONFIG = "com.ustadmobile.gopts"

    }

}