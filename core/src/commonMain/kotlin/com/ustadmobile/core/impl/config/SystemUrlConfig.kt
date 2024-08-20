package com.ustadmobile.core.impl.config

/**
 * Represents the System Url configuration.
 *
 * Here, a 'system' refers to a branch of Ustad Mobile; including the backend HTTP server and
 * clients for Android, desktop, and the web. Each system may contain multiple learning spaces.
 * Configurations options are:
 *
 * This is set in buildconfig.properties and provided via UstadBuildConfig, which is available
 * through dependency injection.
 *
 * @param systemBaseUrl In the perfect world, no system base URL would be needed. The user could just
 * use any learning space URL they want and that would be it, however:
 * - Opening a link on Android 12+ requires declaring a specific domain in the AndroidManifest and
 *   verifying domain ownership as per https://developer.android.com/training/app-links/verify-android-applinks
 *   in order for the Android system to open links using the app.
 * - Using passkeys also requires using verified applinks declared in the AndroidManifest.xml.
 *
 * The Learning Space URL(s) should either be the same as the system base URL, or subdomains thereof.
 *
 * The System Base URL also provides REST API endpoints to list available learning spaces.
 *
 * @param passkeyRpId The passkeys relying party ID : the domain name (without protocol and slash),
 * as per the primary URL (e.g. if the Primary URL is https://example.org/, then the rpId is example.org).
 *
 * @param presetLearningSpaceUrl If a system has only one learning space, then there is no need to
 * ask the user to select a learning space.
 *
 * @param newPersonalAccountsLearningSpaceUrl If enabled, then the client app will show a 'personal
 * account' option for users if they select to create a new account. Some use cases (e.g. content
 * access by individuals) shouldn't include features for interacting with other users.
 */
data class SystemUrlConfig(
    val systemBaseUrl: String,
    val passkeyRpId: String,
    val presetLearningSpaceUrl: String? = null,
    val newPersonalAccountsLearningSpaceUrl: String? = null,
) {

    val canSelectServer: Boolean = presetLearningSpaceUrl == null


    companion object {
        fun fromUstadBuildConfig(buildConfig: UstadBuildConfig): SystemUrlConfig {
            return SystemUrlConfig(
                systemBaseUrl = buildConfig[UstadBuildConfig.KEY_SYSTEM_URL]!!,
                passkeyRpId = buildConfig[UstadBuildConfig.KEY_PASSKEY_RP_ID]!!,
                presetLearningSpaceUrl = buildConfig[UstadBuildConfig.KEY_PRESET_LEARNING_SPACE_URL]
                    ?.takeIf { it.isNotBlank() },
                newPersonalAccountsLearningSpaceUrl = buildConfig[UstadBuildConfig.KEY_NEW_PERSONAL_ACCOUNT_LEARNING_SPACE_URL]
                    ?.takeIf { it.isNotBlank() },
            )
        }
    }

}