package com.ustadmobile.lib.rest.domain.learningspace

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.xxhashkmp.XXStringHasher
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.ihttp.ktorserver.clientUrl
import com.ustadmobile.systemdb.sqlite.SystemDb
import io.github.aakira.napier.Napier
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.server.routing.Route
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.cacheControl
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.on

fun Route.SystemConfigScriptRoute(
    systemDb: SystemDb,
    xxStringHasher: XXStringHasher,
) {
    get("script") {
        try {
            val clientUrl = call.request.clientUrl()
            val baseUrl = clientUrl.replace("api/sysconfig/script", "")

            val learningSpace = systemDb.learningSpaceQueries.findByUid(
                uid = xxStringHasher.hash(baseUrl)
            ).executeAsOneOrNull()

            val (learningSpaceExists, registrationAllowed) = if (learningSpace != null) {
                val di: DI by closestDI()
                val db: UmAppDatabase by di.on(call).instance(tag = DoorTag.TAG_DB)
                val isRegistrationAllowed = db.siteDao().getSiteAsync()?.registrationAllowed
                Pair(true, isRegistrationAllowed ?: false)
            } else {
                Pair(false, false)
            }

            call.response.cacheControl(CacheControl.MaxAge(3_600))
            call.respondText(
                contentType = ContentType.Text.JavaScript,
            ) {
                "var _ustadLearningSpaceExists = $learningSpaceExists;\n"+
                "var _ustadRegistrationAllowed = $registrationAllowed;"
            }
        } catch (e: Throwable) {
            Napier.d { "ustadLearningSpaceExistsErr:-  ${e.message}" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
