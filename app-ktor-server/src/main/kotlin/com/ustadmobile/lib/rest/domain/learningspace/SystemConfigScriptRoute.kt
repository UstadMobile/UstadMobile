package com.ustadmobile.lib.rest.domain.learningspace

import com.ustadmobile.centralappconfigdb.sqlite.CentralAppConfigDb
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.xxhashkmp.XXStringHasher
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.config.UstadBuildConfig
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.ihttp.ktorserver.clientUrl
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
    systemDb: CentralAppConfigDb,
    xxStringHasher: XXStringHasher,
) {
    get("script") {
        try {
            val clientUrl = call.request.clientUrl()
            val baseUrl = clientUrl.replace("api/sysconfig/script", "")
            val matomoApiUrl = UstadBuildConfig.MATOMO_API_URL

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
                "var _ustadRegistrationAllowed = $registrationAllowed;"+
                "var _paq = window._paq = window._paq || [];\n" +
                "        _paq.push(['setCustomUrl', 'https://yourdomain.com/your-new-page-url']);\n"+
                "        _paq.push(['setDocumentTitle', \"<?php echo myPageTitle ?>\"]);\n"+
                "        _paq.push(['trackPageView']);\n" +
                "        _paq.push(['enableLinkTracking']);\n" +
                "        (function() {\n" +
                "          var u= ${matomoApiUrl}\n" +
                "          _paq.push(['setTrackerUrl', u]);\n" +
                "          _paq.push(['setSiteId', '1']);\n" +
                "          var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];\n" +
                "          g.async=true; g.src=u+'matomo.js'; s.parentNode.insertBefore(g,s);\n" +
                "        })();"
            }
        } catch (e: Throwable) {
            Napier.d { "ustadLearningSpaceExistsErr:-  ${e.message}" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
