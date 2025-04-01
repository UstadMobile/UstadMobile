package com.ustadmobile.core.domain.theme

interface ProcessThemeFilesUseCase {

    /**
     * Process extracted theme files and organization details
     *
     * @param extractedDir Path to directory with extracted theme files
     * @param orgName Organization name (optional)
     * @param orgLogo Organization logo URL (optional)
     * @param muiThemePath Path to MUI theme file (optional)
     * @return Result indicating success or failure
     */
    suspend fun invoke(
        extractedDir: String,
        orgName: String? = null,
        orgLogo: String? = null,
        muiThemePath: String? = null
    ): Result<Unit>
}