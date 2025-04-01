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

