package com.ustadmobile.lib.rest.domain.theme

import com.ustadmobile.core.domain.theme.ProcessThemeFilesUseCase
import com.ustadmobile.core.domain.theme.ThemeUploadUseCase
import com.ustadmobile.core.domain.theme.UnzipFileUseCase
import io.github.aakira.napier.Napier
import java.io.File
import java.util.*

/**
 * Implementation of ThemeUploadUseCase that handles theme file processing and GitHub integration
 */
class ThemeUploadUseCaseImpl(
    private val unzipFileUseCase: UnzipFileUseCase,
    private val processThemeFilesUseCase: ProcessThemeFilesUseCase
) : ThemeUploadUseCase {

    override suspend fun invoke(
        request: ThemeUploadUseCase.ThemeUploadRequest,
        jetpackThemeFile: String?,
        muiThemeFile: String?,
        jetpackComposeThemeName: String?,
        muiThemeName: String?
    ): Result<ThemeUploadUseCase.ThemeUploadResponse> {
        try {
            Napier.d("Theme upload initiated with:")
            Napier.d("Organization name: ${request.orgName}")
            Napier.d("Organization logo: ${request.orgLogo}")
            Napier.d("Jetpack theme file: $jetpackThemeFile")
            Napier.d("Jetpack theme name: $jetpackComposeThemeName")
            Napier.d("MUI theme file: $muiThemeFile")
            Napier.d("MUI theme name: $muiThemeName")

            var extractDir: String? = null

            if (jetpackThemeFile != null) {
                val zipFile = File(jetpackThemeFile)
                if (!zipFile.exists()) {
                    return Result.failure(Exception("Jetpack Compose theme file not found"))
                }

                Napier.d("Processing Jetpack Compose theme: ${zipFile.absolutePath}")

                val tempDir = System.getProperty("java.io.tmpdir")
                extractDir = "$tempDir/theme_extract_${UUID.randomUUID()}"
                File(extractDir).mkdirs()

                val unzipResult = unzipFileUseCase.invoke(
                    zipFile.absolutePath,
                    "file://$extractDir"
                )

                if (unzipResult.isFailure) {
                    val exception = unzipResult.exceptionOrNull()
                    Napier.e("Failed to unzip theme file", exception)
                    return Result.failure(exception ?: Exception("Failed to unzip theme file"))
                }
            }

            val processResult = processThemeFilesUseCase.invoke(
                extractedDir = extractDir ?: "",
                orgName = request.orgName,
                orgLogo = request.orgLogo,
                muiThemePath = muiThemeFile,
                jetpackComposeThemeName = jetpackComposeThemeName,
                muiThemeName = muiThemeName
            )

            if (processResult.isFailure) {
                val exception = processResult.exceptionOrNull()
                Napier.e("Failed to process theme files", exception)
                return Result.failure(exception ?: Exception("Failed to process theme files"))
            }

            return Result.success(
                ThemeUploadUseCase.ThemeUploadResponse(
                    success = true,
                    message = "Theme and organization details updated successfully"
                )
            )
        } catch (e: Exception) {
            Napier.e("Theme upload failed", e)
            return Result.failure(e)
        }
    }
}