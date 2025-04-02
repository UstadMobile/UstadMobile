package com.ustadmobile.core.domain.theme

import kotlinx.serialization.Serializable

/**
 * Interface for handling theme file uploads and processing from admin panel
 */
interface ThemeUploadUseCase {

    @Serializable
    data class ThemeUploadRequest(
        val orgName: String? = null,
        val orgLogo: String? = null,
    )

    @Serializable
    data class ThemeUploadResponse(
        val success: Boolean,
        val message: String
    )

    suspend operator fun invoke(
        request: ThemeUploadRequest,
        jetpackThemeFile: String? = null,
        muiThemeFile: String? = null,
        jetpackComposeThemeName: String?,
        muiThemeName: String?
    ): Result<ThemeUploadResponse>
}
