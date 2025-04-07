package com.ustadmobile.lib.rest.domain.theme

import com.ustadmobile.core.domain.theme.ProcessThemeFilesUseCase
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Base64
import java.util.concurrent.TimeUnit

/**
 * Server-side implementation of ProcessThemeFilesUseCase that processes theme files and pushes to GitHub
 */


class ProcessThemeFilesUseCaseImpl : ProcessThemeFilesUseCase {
    companion object {
        private const val GITHUB_TOKEN = "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"
        private const val REPO_OWNER = "UstadMobile"
        private const val REPO_NAME = "UstadMobile"
        private const val BRANCH = "dev-admin-branding-customization-testing"
        private const val STRINGS_XML_PATH = "core/src/commonMain/resources/MR/base/strings.xml"
        private const val THEME_KT_PATH = "lib-ui-compose/src/commonMain/kotlin/com/ustadmobile/libuicompose/theme/Theme.kt"
        private const val COLOR_KT_PATH = "lib-ui-compose/src/commonMain/kotlin/com/ustadmobile/libuicompose/theme/Color.kt"
        private const val ANDROID_LOGO_TARGET_PATH = "app-android/src/main/res/drawable/ic_launcher_icon.xml"
        private const val WEB_LOGO_TARGET_PATH = "app-react/src/jsMain/resources/assets/logo.svg"
        private const val DESKTOP_LOGO_TARGET_PATH = "lib-ui-compose/src/desktopMain/resources/img/logo.svg"
        private const val MUI_THEME_KT_PATH = "app-react/src/jsMain/kotlin/com/ustadmobile/mui/theme/Themes.kt"

        private val httpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    override suspend fun invoke(
        extractedDir: String,
        orgName: String?,
        orgLogo: String?,
        muiThemePath: String?,
        jetpackComposeThemeName: String?,
        muiThemeName: String?
    ): Result<Unit> {
        return try {
            Napier.d("Processing with: extractedDir=$extractedDir, orgName=$orgName, logo=$orgLogo")
            Napier.d("Theme names: jetpackComposeThemeName=$jetpackComposeThemeName, muiThemeName=$muiThemeName")

            if (extractedDir.isNotBlank()) {
                val dir = File(extractedDir)
                Napier.d("Extracted dir exists: ${dir.exists()}")
            }

            if (!orgLogo.isNullOrBlank()) {
                val logoFile = File(orgLogo)
                Napier.d("Logo file exists: ${logoFile.exists()}")
            }

            val changedFiles = mutableMapOf<String, String>()

            //Jetpack theme files if directory exists
            if (extractedDir.isNotBlank() && File(extractedDir).exists()) {
                try {
                    val jetpackFiles = processJetpackThemeFiles(extractedDir)
                    jetpackFiles.forEach { (path, content) ->
                        changedFiles[path] = content
                    }
                    Napier.d("Added ${jetpackFiles.size} theme files to be pushed")
                } catch (e: Exception) {
                    Napier.e("Failed to process theme files: ${e.message}", e)
                }
            }

            // Update strings.xml if orgName or theme names are provided
            if (!orgName.isNullOrBlank() || !jetpackComposeThemeName.isNullOrBlank() || !muiThemeName.isNullOrBlank()) {
                Napier.d("Updating strings.xml with: orgName=$orgName, jetpackTheme=$jetpackComposeThemeName, muiTheme=$muiThemeName")
                try {
                    val (currentContent, _) = fetchStringXmlContent() ?: return Result.failure(Exception("Failed to fetch strings.xml"))
                    val updatedContent = updateStringsXml(
                        orgName,
                        jetpackComposeThemeName,
                        muiThemeName,
                        currentContent
                    )
                    changedFiles[STRINGS_XML_PATH] = updatedContent
                    Napier.d("Added strings.xml to be pushed with updated content")
                } catch (e: Exception) {
                    Napier.e("Failed to update strings.xml: ${e.message}", e)
                }
            }

            // organization logo if provided
            if (!orgLogo.isNullOrBlank() && File(orgLogo).exists()) {
                try {
                    val logoContent = processLogoFile(orgLogo)
                    if (logoContent != null) {
                        changedFiles[ANDROID_LOGO_TARGET_PATH] = logoContent
                        changedFiles[WEB_LOGO_TARGET_PATH] = logoContent
                        changedFiles[DESKTOP_LOGO_TARGET_PATH] = logoContent
                        Napier.d("Added logo to be pushed to Android, Web, and Desktop")
                    }
                } catch (e: Exception) {
                    Napier.e("Failed to process logo: ${e.message}", e)
                }
            }

            // MUI theme if provided
            if (!muiThemePath.isNullOrBlank() && File(muiThemePath).exists()) {
                try {
                    val muiThemeContent = processMuiThemeJson(muiThemePath)
                    if (muiThemeContent != null) {
                        changedFiles[MUI_THEME_KT_PATH] = muiThemeContent
                        Napier.d("Added MUI theme to be pushed")
                    }
                } catch (e: Exception) {
                    Napier.e("Failed to process MUI theme: ${e.message}", e)
                }
            }

            // Push all changed files to GitHub
            if (changedFiles.isNotEmpty()) {
                Napier.d("Pushing ${changedFiles.size} files to GitHub")

                // Log what files are being pushed
                changedFiles.keys.forEach { path ->
                    Napier.d("   - Will push: $path (${changedFiles[path]?.length} chars)")
                }

                for ((filePath, content) in changedFiles) {
                    try {
                        val fileSha = getFileSha(filePath)
                        val result = pushFileToGitHub(content, filePath, "Update $filePath", fileSha)
                        Napier.d("Push result for $filePath: $result")
                    } catch (e: Exception) {
                        Napier.e("Failed to push $filePath: ${e.message}", e)
                    }
                }
            } else {
                Napier.w("No files to push")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("Exception during processing: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Process the Jetpack Compose theme files from the extracted directory
     */
    private fun processJetpackThemeFiles(extractedDir: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        Napier.d("Processing Jetpack theme files from: $extractedDir")

        val rootDir = File(extractedDir)

        Napier.d("Directory contents:")
        rootDir.walkTopDown().forEach { file ->
            Napier.d("  - ${file.absolutePath}")
        }

        // Search for Theme.kt and Color.kt anywhere in the extracted directory
        val themeFiles = mutableListOf<File>()
        val colorFiles = mutableListOf<File>()

        rootDir.walkTopDown().forEach { file ->
            when (file.name) {
                "Theme.kt" -> themeFiles.add(file)
                "Color.kt" -> colorFiles.add(file)
            }
        }

        Napier.d("Found ${themeFiles.size} Theme.kt files and ${colorFiles.size} Color.kt files")

        if (themeFiles.isEmpty() || colorFiles.isEmpty()) {
            val errorMsg = "Theme.kt or Color.kt not found in ${rootDir.absolutePath}"
            throw IllegalArgumentException(errorMsg)
        }

        // Use the first found files
        val themeFile = themeFiles.first()
        val colorFile = colorFiles.first()

        Napier.d("Using Theme.kt from: ${themeFile.absolutePath}")
        Napier.d("Using Color.kt from: ${colorFile.absolutePath}")

        try {
            val themeContent = themeFile.readText()
            Napier.d("Theme.kt content length: ${themeContent.length}")

            val modifiedThemeContent = processThemeContent(themeContent)
            Napier.d("Modified Theme.kt content length: ${modifiedThemeContent.length}")

            val colorContent = colorFile.readText()
            Napier.d("Color.kt content length: ${colorContent.length}")

            val modifiedColorContent = colorContent.replace(
                "package com.example.compose",
                "package com.ustadmobile.libuicompose.theme"
            )
            Napier.d("Modified Color.kt content length: ${modifiedColorContent.length}")

            result[THEME_KT_PATH] = modifiedThemeContent
            result[COLOR_KT_PATH] = modifiedColorContent

            Napier.d("Successfully processed theme files")
            return result
        } catch (e: Exception) {
            Napier.e("Error processing theme files: ${e.message}", e)
            throw e
        }
    }

    /**
     * Process the MUI theme JSON file and extract color values
     */
    private fun processMuiThemeJson(muiThemePath: String): String? {
        try {
            Napier.d("Processing MUI theme from: $muiThemePath")

            val muiThemeFile = File(muiThemePath)

            if (!muiThemeFile.exists()) {
                Napier.e("MUI theme file not found: $muiThemePath")
                return null
            }

            // First fetch the current content to preserve existing values
            val (currentContent, _) = fetchCurrentThemesKtContent() ?: return null

            // Parse the new theme JSON
            val jsonContent = muiThemeFile.readText()
            val jsonObject = Json.parseToJsonElement(jsonContent).jsonObject

            // Extract colors from JSON if available
            val lightScheme = jsonObject["schemes"]?.jsonObject?.get("light")?.jsonObject
            val primaryColor = lightScheme?.get("primary")?.jsonPrimitive?.contentOrNull
            val secondaryColor = lightScheme?.get("onSecondary")?.jsonPrimitive?.contentOrNull

            Napier.d("Extracted colors - primary: $primaryColor, secondary: $secondaryColor")

            // Parse current content to find existing colors
            val primaryPattern = """primary\s*=\s*json\(\s*"main"\s*to\s*Color\("([^"]+)"\)""".toRegex()
            val secondaryPattern = """secondary\s*=\s*json\(\s*"main"\s*to\s*Color\("([^"]+)"\)""".toRegex()

            val currentPrimary = primaryPattern.find(currentContent)?.groupValues?.get(1)
            val currentSecondary = secondaryPattern.find(currentContent)?.groupValues?.get(1)

            // Use new colors if available, otherwise keep current ones
            val finalPrimary = primaryColor ?: currentPrimary ?: "#00796b" // Fallback to default if nothing found
            val finalSecondary = secondaryColor ?: currentSecondary ?: "#ff9800" // Fallback to default if nothing found

            // Create the updated content
            val themesKtContent = currentContent
                .replace(primaryPattern, """primary = json("main" to Color("$finalPrimary")""")
                .replace(secondaryPattern, """secondary = json("main" to Color("$finalSecondary")""")

            Napier.d("Successfully processed MUI theme JSON")
            return themesKtContent
        } catch (e: Exception) {
            Napier.e("Error processing MUI theme: ${e.message}", e)
            return null
        }
    }

    /**
     * Update theme names in strings.xml
     */
    private fun updateThemeNamesInStringsXml(
        jetpackThemeName: String?,
        muiThemeName: String?,
        currentContent: String
    ): String {
        var updatedContent = currentContent

        // Update or add Jetpack Compose theme name
        if (!jetpackThemeName.isNullOrBlank()) {
            val jetpackThemeNamePattern = "<string name=\"jetpack_compose_theme_name\">.*</string>".toRegex()
            if (jetpackThemeNamePattern.containsMatchIn(updatedContent)) {
                updatedContent = updatedContent.replace(jetpackThemeNamePattern,
                    "<string name=\"jetpack_compose_theme_name\">$jetpackThemeName</string>")
                Napier.d("Updated Jetpack Compose theme name to: $jetpackThemeName")
            } else {
                updatedContent = updatedContent.replace(
                    "</resources>",
                    "    <string name=\"jetpack_compose_theme_name\">$jetpackThemeName</string>\n</resources>"
                )
                Napier.d("Added Jetpack Compose theme name: $jetpackThemeName")
            }
        }

        // Update or add MUI theme name
        if (!muiThemeName.isNullOrBlank()) {
            val muiThemeNamePattern = "<string name=\"mui_theme_name\">.*</string>".toRegex()
            if (muiThemeNamePattern.containsMatchIn(updatedContent)) {
                updatedContent = updatedContent.replace(muiThemeNamePattern,
                    "<string name=\"mui_theme_name\">$muiThemeName</string>")
                Napier.d("Updated MUI theme name to: $muiThemeName")
            } else {
                updatedContent = updatedContent.replace(
                    "</resources>",
                    "    <string name=\"mui_theme_name\">$muiThemeName</string>\n</resources>"
                )
                Napier.d("Added MUI theme name: $muiThemeName")
            }
        }

        return updatedContent
    }

    /**
     * Fetch current Themes.kt content from GitHub
     */
    private fun fetchCurrentThemesKtContent(): Pair<String, String?>? {
        val fileUrl = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/contents/$MUI_THEME_KT_PATH?ref=$BRANCH"

        val request = Request.Builder()
            .url(fileUrl)
            .header("Authorization", "token $GITHUB_TOKEN")
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Napier.e("Failed to fetch current Themes.kt: ${response.code}")
                return null
            }

            val responseBody = response.body?.string() ?: ""

            // Extract content
            val contentPattern = "\"content\":\\s*\"([^\"]+)\"".toRegex()
            val contentMatch = contentPattern.find(responseBody)
            val encodedContent = contentMatch?.groupValues?.get(1)?.replace("\\n", "") ?: return null

            val content = String(Base64.getDecoder().decode(encodedContent))

            // Extract SHA
            val shaPattern = "\"sha\":\\s*\"([^\"]+)\"".toRegex()
            val shaMatch = shaPattern.find(responseBody)
            val sha = shaMatch?.groupValues?.get(1)

            return Pair(content, sha)
        }
    }

    /**
     * Process the logo file and convert to SVG if needed
     */
    private fun processLogoFile(logoPath: String): String? {
        try {
            Napier.d("Processing logo from: $logoPath")

            val logoFile = File(logoPath)
            if (!logoFile.exists()) {
                Napier.e("Logo file not found: $logoPath")
                return null
            }

            Napier.d("Logo file exists, size: ${logoFile.length()} bytes")

            // Read the file bytes
            val logoBytes = logoFile.readBytes()
            Napier.d("Read ${logoBytes.size} bytes from logo")

            // Determine MIME type based on file extension
            val mimeType = when {
                logoPath.endsWith(".png", ignoreCase = true) -> "image/png"
                logoPath.endsWith(".jpg", ignoreCase = true) ||
                        logoPath.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                logoPath.endsWith(".svg", ignoreCase = true) -> "image/svg+xml"
                else -> "image/png"
            }

            // If already SVG, just return the content
            if (logoPath.endsWith(".svg", ignoreCase = true)) {
                return String(logoBytes)
            }

            // For non-SVG, encode as base64 and embed in SVG
            val base64Content = Base64.getEncoder().encodeToString(logoBytes)
            val svgWrapper = """
        <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100">
          <image href="data:${mimeType};base64,${base64Content}" width="100" height="100"/>
        </svg>
        """.trimIndent()

            Napier.d("Created SVG with embedded image, length: ${svgWrapper.length}")
            return svgWrapper
        } catch (e: Exception) {
            Napier.e("Error processing logo: ${e.message}", e)
            return null
        }
    }

    /**
     * Process Theme.kt content by replacing package names and method definitions
     */
    private fun processThemeContent(themeContent: String): String {
        return themeContent
            .replace("package com.example.compose", "package com.ustadmobile.libuicompose.theme")
            .replace("import android.app.Activity", "")
            .replace("import android.os.Build", "")
            .replace("import androidx.compose.material3.dynamicDarkColorScheme", "")
            .replace("import androidx.compose.material3.dynamicLightColorScheme", "")
            .replace("import androidx.compose.ui.platform.LocalContext", "")
            .replace(
                """@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
  val colorScheme = when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      
      darkTheme -> darkScheme
      else -> lightScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    content = content
  )
}""",
                """@Composable
fun UstadAppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (!useDarkTheme) {
        lightScheme
    } else {
        darkScheme
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}"""
            )
    }

    /**
     * Update strings.xml with organization name and theme names
     */
    private fun updateStringsXml(
        orgName: String?,
        jetpackThemeName: String?,
        muiThemeName: String?,
        currentContent: String
    ): String {
        try {
            Napier.d("Updating strings.xml with: orgName=$orgName, jetpackTheme=$jetpackThemeName, muiTheme=$muiThemeName")

            var updatedContent = currentContent

            // Update app_name if orgName is provided
            if (!orgName.isNullOrBlank()) {
                val appNamePattern = "<string name=\"app_name\">.*</string>".toRegex()
                if (appNamePattern.containsMatchIn(updatedContent)) {
                    updatedContent = updatedContent.replace(appNamePattern, "<string name=\"app_name\">$orgName</string>")
                    Napier.d("Updated app_name to: $orgName")
                } else {
                    updatedContent = updatedContent.replace(
                        "</resources>",
                        "    <string name=\"app_name\">$orgName</string>\n</resources>"
                    )
                    Napier.d("Added app_name: $orgName")
                }
            }

            // Update theme names
            updatedContent = updateThemeNamesInStringsXml(jetpackThemeName, muiThemeName, updatedContent)

            // Log the updated content excerpt
            Napier.d("Updated strings.xml content (excerpt): ${updatedContent.take(200)}...")

            return updatedContent
        } catch (e: Exception) {
            Napier.e("Error updating strings.xml: ${e.message}", e)
            return currentContent
        }
    }

    /**
     * Fetch strings.xml content from GitHub
     */
    private fun fetchStringXmlContent(): Pair<String, String?>? {
        val fileUrl = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/contents/$STRINGS_XML_PATH?ref=$BRANCH"

        val request = Request.Builder()
            .url(fileUrl)
            .header("Authorization", "token $GITHUB_TOKEN")
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Napier.e("Failed to fetch strings.xml: ${response.code}")
                return null
            }

            val responseBody = response.body?.string() ?: ""

            // Extract content
            val contentPattern = "\"content\":\\s*\"([^\"]+)\"".toRegex()
            val contentMatch = contentPattern.find(responseBody)
            val encodedContent = contentMatch?.groupValues?.get(1)?.replace("\\n", "") ?: return null

            val content = String(Base64.getDecoder().decode(encodedContent))

            // Extract SHA
            val shaPattern = "\"sha\":\\s*\"([^\"]+)\"".toRegex()
            val shaMatch = shaPattern.find(responseBody)
            val sha = shaMatch?.groupValues?.get(1)

            return Pair(content, sha)
        }
    }

    /**
     * Get the SHA of a file in GitHub to enable updating existing files
     */
    private fun getFileSha(filePath: String): String? {
        val fileUrl = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/contents/$filePath?ref=$BRANCH"

        val request = Request.Builder()
            .url(fileUrl)
            .header("Authorization", "token $GITHUB_TOKEN")
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return null
            }

            val responseBody = response.body?.string() ?: return null

            val shaPattern = "\"sha\":\\s*\"([^\"]+)\"".toRegex()
            val shaMatch = shaPattern.find(responseBody)
            return shaMatch?.groupValues?.get(1)
        }
    }

    /**
     * Push file content to GitHub
     */
    private fun pushFileToGitHub(
        fileContent: String,
        filePath: String,
        commitMessage: String,
        existingSha: String?
    ): String {
        try {
            Napier.d("Pushing file to GitHub: $filePath")

            // Encode file content to base64
            val base64Content = Base64.getEncoder().encodeToString(fileContent.toByteArray())

            val requestBodyJson = buildString {
                append("{")
                append("\"message\": \"$commitMessage\",")
                append("\"branch\": \"$BRANCH\",")
                append("\"content\": \"$base64Content\"")
                if (existingSha != null) {
                    append(",\"sha\": \"$existingSha\"")
                }
                append("}")
            }

            val contentUrl = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/contents/$filePath"
            val request = Request.Builder()
                .url(contentUrl)
                .put(requestBodyJson.toRequestBody("application/json".toMediaType()))
                .header("Authorization", "token $GITHUB_TOKEN")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: "No response body"

                return if (response.isSuccessful) {
                    Napier.d("Successfully pushed file to GitHub: $filePath")
                    "Success! File pushed to GitHub."
                } else {
                    Napier.e("Failed to push file to GitHub: ${response.code} - $responseBody")
                    "Failed to push to GitHub (${response.code}): $responseBody"
                }
            }
        } catch (e: Exception) {
            Napier.e("Exception during GitHub push: ${e.message}", e)
            return "Error during GitHub push: ${e.message}"
        }
    }
}