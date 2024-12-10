package com.ustadmobile.lib.rest.domain.matomo

import com.ustadmobile.core.impl.config.UstadBuildConfig
import io.github.aakira.napier.Napier
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.MatomoConfigRoute() {
    get("matomoscript") {
        try {
            val matomoApiUrl = UstadBuildConfig.MATOMO_API_URL
            call.respondText(contentType = ContentType.Text.JavaScript) {
                """
                 var _paq = window._paq = window._paq || [];
                _paq.push(['trackPageView']);
                _paq.push(['enableLinkTracking']);
                (function() {
                    var u="$matomoApiUrl";
                    _paq.push(['setTrackerUrl', u]);
                    _paq.push(['setSiteId', '1']);
                    var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
                    g.async=true; g.src=u+'matomo.js'; s.parentNode.insertBefore(g,s);
                })();
                """.trimIndent()
            }
        } catch (e: Throwable) {
            Napier.e { "Matomo script generation failed: ${e.message}" }
            call.respondText(
                "console.error('Failed to load Matomo configuration.');",
                contentType = ContentType.Text.JavaScript,
                status = HttpStatusCode.InternalServerError
            )
        }
    }
}
