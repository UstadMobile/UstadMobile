package com.ustadmobile.lib.rest.domain.theme

import com.google.gson.JsonParser
import com.ustadmobile.core.domain.theme.ThemeUploadUseCase
import io.github.aakira.napier.Napier
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*

/**
 * Route handler for theme rebrand functionality
 */
fun Route.themeRoute(
    themeUploadUseCase: (ApplicationCall) -> ThemeUploadUseCase
) {
    route("theme") {

        get("test") {
            Napier.d("Theme test endpoint called")
            val themeContent = ""

            fun saveAndPushToGitHub(themeContent: String): String {
                return try {
                    // 1. Verify these values are correct
                    val githubToken = "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" // Make sure this is valid
                    val repoOwner = "UstadMobile"     // Case-sensitive
                    val repoName = "UstadMobile"            // Case-sensitive
                    val branch = "dev-admin-branding-customization"                    // Verify your branch name

                    val client = OkHttpClient()
                    val filePath = "theme.kt"               // Path where file will be saved
                    val commitMessage = "Add theme.kt via API"
                    val base64Content = Base64.getEncoder().encodeToString(themeContent.toByteArray())

                    // 2. First verify repository exists
                    val repoUrl = "https://api.github.com/repos/$repoOwner/$repoName"
                    val repoRequest = Request.Builder()
                        .url(repoUrl)
                        .header("Authorization", "token $githubToken")
                        .header("Accept", "application/vnd.github.v3+json")
                        .build()

                    val repoResponse = client.newCall(repoRequest).execute()
                    if (!repoResponse.isSuccessful) {
                        return "Repository not found (${repoResponse.code}). Check owner/repo name."
                    }

                    // 3. Create/update file
                    val contentUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/$filePath"
                    val requestBody = """
                {
                    "message": "$commitMessage",
                    "content": "$base64Content",
                    "branch": "$branch"
                }
            """.trimIndent()

                    val request = Request.Builder()
                        .url(contentUrl)
                        .put(requestBody.toRequestBody("application/json".toMediaType()))
                        .header("Authorization", "token $githubToken")
                        .header("Accept", "application/vnd.github.v3+json")
                        .build()

                    client.newCall(request).execute().use { response ->
                        return if (response.isSuccessful) {
                            "Success! File pushed to GitHub."
                        } else {
                            val errorBody = response.body?.string() ?: "No error details"
                            "Failed (${response.code}): $errorBody"
                        }
                    }
                } catch (e: Exception) {
                    Napier.e("GitHub push failed", e)
                    "Error: ${e.message}"
                }
            }

            val pushResult = saveAndPushToGitHub(themeContent)
            call.respond(HttpStatusCode.OK, mapOf(
                "status" to "Theme API test",
                "github_push_result" to pushResult
            ))


        }

        post("upload") {
            try {
                val multipart = call.receiveMultipart()
                var jetpackComposeThemePath: String? = null
                var muiThemePath: String? = null
                var orgName: String? = null
                var orgLogo: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "orgName" -> orgName = part.value
                                "orgLogo" -> orgLogo = part.value
                                "jetpackComposeTheme" -> jetpackComposeThemePath = part.value
                                "muiTheme" -> muiThemePath = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            val fileBytes = part.streamProvider().readBytes()
                            val tempDir = System.getProperty("java.io.tmpdir")
                            val fileName = "${part.name}_${UUID.randomUUID()}"
                            val extension = when (part.name) {
                                "jetpackComposeTheme" -> ".zip"
                                "muiTheme" -> ".json"
                                else -> ".tmp"
                            }

                            val file = File(tempDir, fileName + extension)
                            file.writeBytes(fileBytes)

                            when (part.name) {
                                "jetpackComposeTheme" -> jetpackComposeThemePath = file.absolutePath
                                "muiTheme" -> muiThemePath = file.absolutePath
                            }

                            Napier.d("Saved ${part.name} to ${file.absolutePath}")
                        }
                        else -> {
                        }
                    }
                    part.dispose()
                }

                val request = ThemeUploadUseCase.ThemeUploadRequest(
                    orgName = orgName,
                    orgLogo = orgLogo
                )

                val result = themeUploadUseCase(call).invoke(
                    request,
                    jetpackComposeThemePath,
                    muiThemePath
                )

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response != null && response.success) {
                        call.respond(HttpStatusCode.OK, response)
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ThemeUploadUseCase.ThemeUploadResponse(
                                success = false,
                                message = response?.message ?: "Unknown error"
                            )
                        )
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    Napier.e("Theme upload failed", exception)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ThemeUploadUseCase.ThemeUploadResponse(
                            success = false,
                            message = "Error: ${exception?.message ?: "Unknown error"}"
                        )
                    )
                }
            } catch (e: Exception) {
                Napier.e("Exception in theme upload", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ThemeUploadUseCase.ThemeUploadResponse(
                        success = false,
                        message = "Server error: ${e.message}"
                    )
                )
            }
        }
    }
}

