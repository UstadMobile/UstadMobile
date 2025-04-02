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
                var jetpackComposeThemeName: String? = null
                var muiThemeName: String? = null
                var orgName: String? = null
                var orgLogo: String? = null

                val tempDir = System.getProperty("java.io.tmpdir")
                val uploadDir = File(tempDir, "theme_uploads_${UUID.randomUUID()}")
                uploadDir.mkdirs()

                Napier.d("Created upload directory: ${uploadDir.absolutePath}")

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            Napier.d("Processing form item: ${part.name} = ${part.value}")

                            when (part.name) {
                                "organisationName" -> {
                                    orgName = part.value
                                    Napier.d("Set orgName: $orgName")
                                }
                                "jetpackComposeThemeName" -> {
                                    jetpackComposeThemeName = part.value
                                    Napier.d("Set jetpackComposeThemeName: $jetpackComposeThemeName")
                                }
                                "muiThemeName" -> {
                                    muiThemeName = part.value
                                    Napier.d("Set muiThemeName: $muiThemeName")
                                }
                                else -> {
                                    Napier.d("Unhandled form part: ${part.name}")
                                }
                            }
                        }
                        is PartData.FileItem -> {
                            try {
                                Napier.d("Processing file: ${part.name}, original filename: ${part.originalFileName}")

                                val fileBytes = part.streamProvider().readBytes()
                                if (fileBytes.isEmpty()) {
                                    Napier.w("Warning: Received empty file for ${part.name}")
                                    part.dispose()
                                    return@forEachPart
                                }

                                Napier.d("Read ${fileBytes.size} bytes from ${part.name}")

                                val originalFileName = part.originalFileName ?: "${part.name}_${UUID.randomUUID()}"
                                val fileExtension = originalFileName.substringAfterLast('.', "bin")
                                val fileName = "${part.name}_${UUID.randomUUID()}.$fileExtension"
                                val file = File(uploadDir, fileName)

                                file.writeBytes(fileBytes)
                                Napier.d("Saved ${part.name} to: ${file.absolutePath} (${file.length()} bytes)")

                                when (part.name) {
                                    "jetpackComposeTheme" -> {
                                        jetpackComposeThemePath = file.absolutePath
                                        Napier.d("Set jetpackComposeThemePath: ${file.absolutePath}")
                                    }
                                    "muiTheme" -> {
                                        muiThemePath = file.absolutePath
                                        Napier.d("Set muiThemePath: ${file.absolutePath}")
                                    }
                                    "orgLogo" -> {
                                        orgLogo = file.absolutePath
                                        Napier.d("Set orgLogo: ${file.absolutePath}")
                                    }
                                }
                            } catch (e: Exception) {
                                Napier.e("Error processing file part ${part.name}: ${e.message}", e)
                            }
                        }
                        else -> {
                            Napier.d("Unhandled part type: ${part::class.simpleName}, name: ${part.name}")
                        }
                    }
                    part.dispose()
                }

                Napier.d("- orgName: $orgName")
                Napier.d("- orgLogo: $orgLogo")
                Napier.d("- jetpackComposeThemePath: $jetpackComposeThemePath")
                Napier.d("- muiThemePath: $muiThemePath")
                Napier.d("- jetpackComposeThemeName: $jetpackComposeThemeName")
                Napier.d("- muiThemeName: $muiThemeName")

                val request = ThemeUploadUseCase.ThemeUploadRequest(
                    orgName = orgName,
                    orgLogo = orgLogo
                )
                Napier.d("Calling ThemeUploadUseCase with request: $request")
                val result = themeUploadUseCase(call).invoke(
                    request,
                    jetpackComposeThemePath,
                    muiThemePath,
                    jetpackComposeThemeName,
                    muiThemeName
                )

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response != null && response.success) {
                        Napier.d("Theme upload successful: ${response.message}")
                        call.respond(HttpStatusCode.OK, response)
                    } else {
                        Napier.e("Theme upload failed with error: ${response?.message}")
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ThemeUploadUseCase.ThemeUploadResponse(
                                success = false,
                                message = response?.message ?: "Unknown error occurred during theme processing"
                            )
                        )
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    Napier.e("Theme upload failed with exception", exception)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ThemeUploadUseCase.ThemeUploadResponse(
                            success = false,
                            message = "Error processing theme: ${exception?.message ?: "Unknown error"}"
                        )
                    )
                }

                try {
                    if (uploadDir.exists()) {
                        uploadDir.deleteRecursively()
                        Napier.d("Cleaned up upload directory: ${uploadDir.absolutePath}")
                    }
                } catch (e: Exception) {
                    Napier.e("Failed to clean up upload directory: ${e.message}", e)
                }
            } catch (e: Exception) {
                Napier.e("Unhandled exception in theme upload endpoint", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ThemeUploadUseCase.ThemeUploadResponse(
                        success = false,
                        message = "Server error: ${e.message ?: "Unknown error"}"
                    )
                )
            }
        }
    }
}

