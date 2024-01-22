package com.ustadmobile.core.impl.config

/**
 * Represents the Api Url configuration. This is generally not used on the web version (where the
 * api url is based on the link in the browser).
 *
 * 1. An app that is not tied to any one specific server - the user will be prompted for a link when
 * they open the app
 *
 * 2. An app that is tied to a specific preset server. The user will not be prompted for a link, they
 * will be taken directly to the login screen.
 */
data class ApiUrlConfig(
    val presetApiUrl: String?
) {

    val canSelectServer: Boolean = presetApiUrl == null

}