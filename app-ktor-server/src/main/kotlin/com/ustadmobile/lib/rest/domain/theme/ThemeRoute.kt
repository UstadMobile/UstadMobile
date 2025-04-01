package com.ustadmobile.lib.rest.domain.theme

import com.ustadmobile.core.domain.theme.ThemeUploadUseCase
import io.github.aakira.napier.Napier
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.io.File
import java.util.UUID

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

