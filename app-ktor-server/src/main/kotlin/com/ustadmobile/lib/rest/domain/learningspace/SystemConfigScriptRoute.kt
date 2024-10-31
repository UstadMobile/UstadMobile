package com.ustadmobile.lib.rest.domain.learningspace

import com.ustadmobile.appconfigdb.SystemDb
import com.ustadmobile.ihttp.ktorserver.clientUrl
import io.github.aakira.napier.Napier
import io.ktor.http.ContentType
import io.ktor.server.routing.Route
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get

fun Route.SystemConfigScriptRoute(
    systemDb: SystemDb
) {

    get("script") {

        try {
            val clientUrl = call.request.clientUrl()
            val baseUrl = clientUrl.replace("api/sysconfig/script", "")

            val learningSpace = systemDb.learningSpaceInfoDao().getLearningSpace(baseUrl)

            if (learningSpace != null) {

                call.respondText(contentType = ContentType.Text.JavaScript) {
                    "var _ustadLearningSpaceExists = true;"
                }
            } else {
                call.respond(HttpStatusCode.ExpectationFailed, "Learning space not found.")
            }

        } catch (e: Throwable) {
            Napier.d { "ustadLearningSpaceExistsErr:-  ${e.message}" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
