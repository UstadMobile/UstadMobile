package com.ustadmobile.lib.rest.domain.theme

import com.ustadmobile.core.domain.theme.ProcessThemeFilesUseCase
import io.github.aakira.napier.Napier
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
        private const val GITHUB_TOKEN = ""
        private const val REPO_OWNER = "UstadMobile"
        private const val REPO_NAME = "UstadMobile"
        private const val BRANCH = "dev-admin-branding-customization-testing"
        private const val STRINGS_XML_PATH = "core/src/commonMain/resources/MR/base/strings.xml"
        private const val LOGO_TARGET_PATH = "core/src/commonMain/resources/MR/images/ustad_logo.svg"

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
        muiThemePath: String?
    ): Result<Unit> {
        return try {
            Napier.d("🟢 Processing extractedDir: $extractedDir")
            Napier.d("🟢 Organization name: $orgName, logo: $orgLogo")

            // Track which files need changes
            val changedFiles = mutableMapOf<String, String>()
            val commitMessage = buildCommitMessage(extractedDir, orgName, orgLogo, muiThemePath)

            // Get theme names
            val jetpackThemeName = getJetpackThemeName(extractedDir)
            val muiThemeName = getMuiThemeName(muiThemePath)

            // Process Jetpack theme files if directory exists
            if (extractedDir.isNotEmpty()) {
                val jetpackFiles = processJetpackThemeFiles(extractedDir)
                changedFiles.putAll(jetpackFiles)
            }

            // Update strings.xml only if we have new values
            if (orgName != null || extractedDir.isNotEmpty() || muiThemePath != null) {
                val stringsXmlContent = updateStringsXml(orgName, jetpackThemeName, muiThemeName)
                if (stringsXmlContent != null) {
                    changedFiles[STRINGS_XML_PATH] = stringsXmlContent
                }
            }

            // Handle organization settings if provided
            if (orgName != null || orgLogo != null) {
                val configContent = createOrganizationConfig(orgName, orgLogo)
                changedFiles["config/organization.json"] = configContent
            }

            // Process and handle logo file if provided
            if (!orgLogo.isNullOrEmpty()) {
                val logoContent = processLogoFile(orgLogo)
                if (logoContent != null) {
                    changedFiles[LOGO_TARGET_PATH] = logoContent
                }
            }

            // Handle MUI theme if provided
            if (!muiThemePath.isNullOrEmpty()) {
                val muiContent = processMuiThemeIfExists(muiThemePath)
                if (muiContent != null) {
                    changedFiles["web-ui/src/theme/customTheme.js"] = muiContent
                }
            }

            // Push all changed files in a single batch if possible, or individually if needed
            if (changedFiles.isNotEmpty()) {
                pushChangedFiles(changedFiles, commitMessage)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("❌ Exception during processing: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Process the logo file and return its content
     */
    private fun processLogoFile(logoPath: String): String? {
        try {
            val logoFile = File(logoPath)
            if (logoFile.exists()) {
                Napier.d("✅ Logo file found: $logoPath")
                val logoContent = logoFile.readText()
                Napier.d("✅ Logo file read successfully")
                return logoContent
            } else {
                Napier.w("⚠️ Logo file not found: $logoPath")
            }
        } catch (e: Exception) {
            Napier.e("❌ Error processing logo file: ${e.message}", e)
        }
        return null
    }

    private fun getJetpackThemeName(extractedDir: String): String {
        return if (extractedDir.isNotEmpty()) {
            val dir = File(extractedDir)
            dir.name.takeIf { it.isNotEmpty() } ?: "Custom Jetpack Theme"
        } else {
            "Default Jetpack Theme"
        }
    }

    private fun getMuiThemeName(muiThemePath: String?): String {
        return if (!muiThemePath.isNullOrEmpty()) {
            val file = File(muiThemePath)
            file.nameWithoutExtension.takeIf { it.isNotEmpty() } ?: "Custom MUI Theme"
        } else {
            "Default MUI Theme"
        }
    }

    private fun processJetpackThemeFiles(extractedDir: String): Map<String, String> {
        val themeFile = File(extractedDir, "ui/theme/Theme.kt")
        val colorFile = File(extractedDir, "ui/theme/Color.kt")
        val result = mutableMapOf<String, String>()

        Napier.d("📂 Checking files:")
        Napier.d("📄 Theme.kt exists: ${themeFile.exists()} (${themeFile.absolutePath})")
        Napier.d("📄 Color.kt exists: ${colorFile.exists()} (${colorFile.absolutePath})")

        if (!themeFile.exists() || !colorFile.exists()) {
            Napier.e("❌ theme.kt or color.kt not found!")
            throw IllegalArgumentException("theme.kt or color.kt not found!")
        }

        Napier.d("✅ Processing theme files...")

        // Process Theme.kt
        val themeContent = themeFile.readText()
        val modifiedThemeContent = processThemeContent(themeContent)
        themeFile.writeText(modifiedThemeContent)
        Napier.d("✅ Theme.kt modification done!")

        // Process Color.kt
        val colorContent = colorFile.readText()
        val modifiedColorContent = colorContent.replace(
            "package com.example.compose",
            "package com.ustadmobile.libuicompose.theme"
        )
        colorFile.writeText(modifiedColorContent)
        Napier.d("✅ Color.kt modification done!")

        // Copy files to project directory
        copyFilesToProject(themeFile, colorFile)

        // Add to changed files list
        result["lib-ui-compose/src/commonMain/kotlin/com/ustadmobile/libuicompose/theme/Theme.kt"] = modifiedThemeContent
        result["lib-ui-compose/src/commonMain/kotlin/com/ustadmobile/libuicompose/theme/Color.kt"] = modifiedColorContent

        return result
    }

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

    private fun copyFilesToProject(themeFile: File, colorFile: File) {
        val projectDir = System.getProperty("user.dir")
        val targetThemeDir = File("$projectDir/lib-ui-compose/src/commonMain/kotlin/libuicompose/theme")
        targetThemeDir.mkdirs()

        val targetThemeFile = File(targetThemeDir, "Theme.kt")
        val targetColorFile = File(targetThemeDir, "Color.kt")

        themeFile.copyTo(targetThemeFile, overwrite = true)
        colorFile.copyTo(targetColorFile, overwrite = true)

        Napier.d("✅ Files copied to project directory")
    }

    private fun updateStringsXml(orgName: String?, jetpackThemeName: String, muiThemeName: String): String? {
        try {
            Napier.d("🔄 Updating strings.xml with new values")

            // Fetch current strings.xml content and SHA
            val (currentContent, _) = fetchStringXmlContent() ?: return null

            // Update content with new values
            val modifiedContent = updateStringsContent(currentContent, orgName, jetpackThemeName, muiThemeName)

            // Only return content if it has actually changed
            if (modifiedContent != currentContent) {
                return modifiedContent
            }

            return null
        } catch (e: Exception) {
            Napier.e("❌ Error updating strings.xml: ${e.message}", e)
            return null
        }
    }

    private fun fetchStringXmlContent(): Pair<String, String?>? {
        val fileUrl = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/contents/$STRINGS_XML_PATH?ref=$BRANCH"

        val fileRequest = Request.Builder()
            .url(fileUrl)
            .header("Authorization", "token $GITHUB_TOKEN")
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        httpClient.newCall(fileRequest).execute().use { response ->
            if (!response.isSuccessful) {
                Napier.e("❌ Failed to retrieve strings.xml: ${response.code}")
                return null
            }

            val responseBody = response.body?.string() ?: ""

            // Extract content
            val contentPattern = "\"content\":\\s*\"([^\"]+)\"".toRegex()
            val contentMatch = contentPattern.find(responseBody)
            val encodedContent = contentMatch?.groupValues?.get(1)?.replace("\\n", "")
                ?: return null

            val content = String(Base64.getDecoder().decode(encodedContent))
            Napier.d("✅ Successfully retrieved strings.xml content")

            // Extract SHA
            val shaPattern = "\"sha\":\\s*\"([^\"]+)\"".toRegex()
            val shaMatch = shaPattern.find(responseBody)
            val sha = shaMatch?.groupValues?.get(1)
            Napier.d("🔑 strings.xml SHA: $sha")

            return Pair(content, sha)
        }
    }

    private fun updateStringsContent(
        currentContent: String,
        orgName: String?,
        jetpackThemeName: String,
        muiThemeName: String
    ): String {
        var modifiedContent = currentContent

        // Update app_name if orgName is provided
        if (!orgName.isNullOrEmpty()) {
            val appNamePattern = "<string name=\"app_name\">([^<]+)</string>".toRegex()
            modifiedContent = modifiedContent.replace(appNamePattern, "<string name=\"app_name\">$orgName</string>")
            Napier.d("✅ Updated app_name to: $orgName")
        }

        // Update or add jetpack theme name
        modifiedContent = updateOrAddStringEntry(
            modifiedContent,
            "jetpack_compose_theme_name",
            jetpackThemeName
        )

        // Update or add mui theme name
        modifiedContent = updateOrAddStringEntry(
            modifiedContent,
            "mui_theme_name",
            muiThemeName
        )

        return modifiedContent
    }

    private fun updateOrAddStringEntry(content: String, entryName: String, entryValue: String): String {
        val pattern = "<string name=\"$entryName\">([^<]+)</string>".toRegex()

        return if (pattern.containsMatchIn(content)) {
            // Update existing entry
            val updated = content.replace(pattern, "<string name=\"$entryName\">$entryValue</string>")
            Napier.d("✅ Updated $entryName to: $entryValue")
            updated
        } else {
            // Add new entry before closing tag
            val updated = content.replace(
                "</resources>",
                "    <string name=\"$entryName\">$entryValue</string>\n</resources>"
            )
            Napier.d("✅ Added new entry $entryName: $entryValue")
            updated
        }
    }

    private fun createOrganizationConfig(orgName: String?, orgLogo: String?): String {
        Napier.d("🔄 Creating organization config JSON")

        // Create config file content
        return buildString {
            append("{\n")
            if (orgName != null) {
                append("  \"name\": \"$orgName\"")
                if (orgLogo != null) append(",\n")
            }
            if (orgLogo != null) {
                append("  \"logo\": \"$orgLogo\"")
            }
            append("\n}")
        }
    }

    private fun processMuiThemeIfExists(muiThemePath: String?): String? {
        if (!muiThemePath.isNullOrEmpty()) {
            val muiThemeFile = File(muiThemePath)
            if (muiThemeFile.exists()) {
                Napier.d("✅ MUI theme file found, reading content")
                return muiThemeFile.readText()
            } else {
                Napier.w("⚠️ MUI theme file not found: $muiThemePath")
            }
        }
        return null
    }

    /**
     * Creates a descriptive commit message based on what's being changed
     */
    private fun buildCommitMessage(
        extractedDir: String,
        orgName: String?,
        orgLogo: String?,
        muiThemePath: String?
    ): String {
        val changes = mutableListOf<String>()

        if (extractedDir.isNotEmpty()) {
            changes.add("Jetpack Compose theme")
        }

        if (orgName != null) {
            changes.add("organization name")
        }

        if (orgLogo != null) {
            changes.add("organization logo")
        }

        if (!muiThemePath.isNullOrEmpty()) {
            changes.add("MUI theme")
        }

        return if (changes.isEmpty()) {
            "Update application settings"
        } else {
            "Update " + changes.joinToString(", ")
        }
    }

    /**
     * Push all changed files to GitHub with a single commit message
     */
    private fun pushChangedFiles(changedFiles: Map<String, String>, commitMessage: String) {
        try {
            Napier.d("🔄 Pushing ${changedFiles.size} changed files to GitHub with message: $commitMessage")

            // Currently, the GitHub API doesn't support multi-file commits through the REST API in a simple way
            // So we'll push files individually but with the same commit message
            changedFiles.forEach { (filePath, content) ->
                val fileSha = getFileSha(filePath)
                val result = pushFileToGitHub(content, filePath, commitMessage, fileSha)
                Napier.d("🔄 GitHub push result for $filePath: $result")
            }

            Napier.d("✅ All files pushed successfully")
        } catch (e: Exception) {
            Napier.e("❌ Error pushing files to GitHub: ${e.message}", e)
        }
    }

    private fun pushFileToGitHub(
        fileContent: String,
        filePath: String,
        commitMessage: String,
        existingSha: String? = null
    ): String {
        return try {
            // Encode file content to base64
            val base64Content = Base64.getEncoder().encodeToString(fileContent.toByteArray())

            // Verify repository exists
            val repoUrl = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME"
            if (!checkRepositoryExists(repoUrl)) {
                return "Repository check failed"
            }

            // Get file SHA if needed
            val fileSha = existingSha ?: getFileSha(filePath)

            // Push file
            return pushFileContent(filePath, commitMessage, base64Content, fileSha)

        } catch (e: Exception) {
            Napier.e("❌ Exception during GitHub push", e)
            "Error during GitHub push: ${e.message}"
        }
    }

    private fun checkRepositoryExists(repoUrl: String): Boolean {
        Napier.d("🌐 Checking repository at: $repoUrl")

        val repoRequest = Request.Builder()
            .url(repoUrl)
            .header("Authorization", "token $GITHUB_TOKEN")
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        httpClient.newCall(repoRequest).execute().use { repoResponse ->
            if (!repoResponse.isSuccessful) {
                val errorBody = repoResponse.body?.string() ?: "No error details"
                Napier.e("❌ Repository check failed: ${repoResponse.code} - $errorBody")
                return false
            }

            Napier.d("✅ Repository exists, proceeding to file operation")
            return true
        }
    }

    private fun getFileSha(filePath: String): String? {
        val fileUrl = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/contents/$filePath?ref=$BRANCH"
        Napier.d("🔍 Checking if file exists at: $fileUrl")

        val fileCheckRequest = Request.Builder()
            .url(fileUrl)
            .header("Authorization", "token $GITHUB_TOKEN")
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        httpClient.newCall(fileCheckRequest).execute().use { fileCheckResponse ->
            if (fileCheckResponse.isSuccessful) {
                val responseBody = fileCheckResponse.body?.string()
                if (responseBody != null) {
                    // Extract SHA
                    val shaPattern = "\"sha\":\\s*\"([^\"]+)\"".toRegex()
                    val shaMatch = shaPattern.find(responseBody)
                    val sha = shaMatch?.groupValues?.get(1)
                    Napier.d("🔑 Existing file found, SHA: $sha")
                    return sha
                }
            } else {
                Napier.d("📄 File doesn't exist yet, will create new file")
            }
            return null
        }
    }

    private fun pushFileContent(
        filePath: String,
        commitMessage: String,
        base64Content: String,
        fileSha: String?
    ): String {
        // Create request body
        val requestBody = buildString {
            append("{")
            append("\"message\": \"$commitMessage\",")
            append("\"branch\": \"$BRANCH\",")
            append("\"content\": \"$base64Content\"")
            if (fileSha != null) {
                append(",\"sha\": \"$fileSha\"")
            }
            append("}")
        }

        Napier.d("📝 Preparing to ${if (fileSha != null) "update" else "create"} file at path: $filePath")

        val contentUrl = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/contents/$filePath"
        val request = Request.Builder()
            .url(contentUrl)
            .put(requestBody.toRequestBody("application/json".toMediaType()))
            .header("Authorization", "token $GITHUB_TOKEN")
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        httpClient.newCall(request).execute().use { response ->
            return if (response.isSuccessful) {
                Napier.d("✅ Successfully pushed file to GitHub")
                "Success! File pushed to GitHub."
            } else {
                val errorBody = response.body?.string() ?: "No error details"
                Napier.e("❌ GitHub push failed: ${response.code} - $errorBody")
                "Failed to push to GitHub (${response.code}): $errorBody"
            }
        }
    }
}