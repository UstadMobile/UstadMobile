package com.ustadmobile.lib.rest.domain.learningspace

import com.ustadmobile.appconfigdb.SystemDb
import com.ustadmobile.appconfigdb.SystemDbDataLayer
import io.ktor.http.ContentType
import io.ktor.server.routing.Route
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

fun Route.SystemConfigScriptRoute(
    di: DI
) {

    post("script") {
        val clientUrl = call.request.uri
        val baseUrl = clientUrl.replace("api/sysconfig/script", "")
        val repo: SystemDb? = di.direct.instance<SystemDbDataLayer>().repository
        if (repo != null) {
            val learningSpace = repo.learningSpaceInfoDao().getLearningSpace(clientUrl)

            if (learningSpace != null) {

                call.respondText(contentType = ContentType.Text.JavaScript) {
                    "var _ustadLearningSpaceExists = true;"
                }
            } else {
                call.respond(HttpStatusCode.NotFound, "Learning space not found.")
            }
        } else {
            call.respond(HttpStatusCode.NotFound, "Repo is null")
        }


    }
}