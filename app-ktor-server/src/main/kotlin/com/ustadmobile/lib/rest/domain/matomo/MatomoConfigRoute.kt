package com.ustadmobile.lib.rest.domain.matomo

import com.ustadmobile.core.impl.config.UstadBuildConfig
import io.github.aakira.napier.Napier
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.MatomoConfigRoute() {
    get("matomoscript") {
        try {
            call.respondText(contentType = ContentType.Text.JavaScript) {
                """
                console.log("Matomo tracking initialized");
                var _paq = window._paq = window._paq || [];
                _paq.push(['setDocumentTitle', "Home"]);
                _paq.push(['trackPageView']);
                _paq.push(['enableLinkTracking']);
                (function() {
                    var u = "${UstadBuildConfig.MATOMO_API_URL}";
                    _paq.push(['setTrackerUrl', u]);
                    _paq.push(['setSiteId', '1']);
                    var d = document, g = d.createElement('script'), s = d.getElementsByTagName('script')[0];
                    g.async = true; g.src = u + 'matomo.js'; s.parentNode.insertBefore(g, s);
                })();
                """.trimIndent()
            }
        } catch (e: Throwable) {
            Napier.d { "matomoUrlExistsErr: ${e.message}" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
