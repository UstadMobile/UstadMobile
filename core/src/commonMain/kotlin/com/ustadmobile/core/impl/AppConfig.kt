package com.ustadmobile.core.impl

/**
 * Constants that represent keys used in appconfig.properties
 */
object AppConfig {

    const val KEY_CONTENT_DIR_NAME = "content_dir"

    const val KEY_LOGIN_REQUIRED_FOR_CONTENT_OPEN = "app.login_before_open"

    const val KEY_LOGIN_REQUIRED_FOR_CONTENT_DOWNLOAD = "login_before_download"

    const val KEY_FIRST_DEST = "first_dest"

    const val KEY_FIRST_DEST_LOGIN_REQUIRED = "first_dest_login_required"

    const val KEY_API_URL = "apiUrl"

    const val KEY_APP_BASE_NAME = "appBaseName"

    const val KEY_SUPPORTED_LANGUAGES = "app.ui_languages"

    const val KEY_DEFAULT_LANGUAGE = "app.ui_default_language"

    const val KEY_ALLOW_GUEST_LOGIN= "app.guest_allowed"

    const val KEY_ALLOW_REGISTRATION = "app.register_allowed"

    const val KEY_ALLOW_SERVER_SELECTION = "app.select_server"

    const val KEY_NO_IFRAME = "app.no_iframe_domans"

    const val KEY_CONTENT_ONLY_MODE = "app.content_only_mode"

    const val KEY_PBKDF2_ITERATIONS = "pbkdf2.iterations"

    const val KEY_PBKDF2_KEYLENGTH = "pbkdf2.keylength"

}
