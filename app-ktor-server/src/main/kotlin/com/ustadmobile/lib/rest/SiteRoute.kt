package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Site
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import org.kodein.di.on

fun Route.SiteRoute() {
    route("Site") {
        get("verify") {
            val _di = closestDI()
            val db: UmAppDatabase by _di.on(call).instance(tag = DoorTag.TAG_DB)

            //Make sure the repo has been initialized
            _di.on(call).direct.instance<UmAppDatabase>(tag = DoorTag.TAG_REPO)

            val site = db.siteDao.getSite()
            call.respond(if(site != null)  HttpStatusCode.OK else HttpStatusCode.NotFound,
                    site?: Site())
        }
    }
}
