package com.ustadmobile.lib.rest

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Route.GetAppRoute() {
    get("/getapp/") {
        val config = this.context.application.environment.config
        val di : DI by closestDI()
        val systemImpl : UstadMobileSystemImpl = di.direct.instance()

        call.respondHtml {
            head {
                title {
                    +"Get the app"
                }
            }

            body {
                h1 {
                    +"Get the app"
                }

                p {
                    + "Please get the ${systemImpl.getString(MessageID.app_name, Any())} app to open this link."
                    br {  }
                    a(href = config.propertyOrNull("ktor.ustad.androidDownloadHref")?.getString() ?: "#") {
                        + "Download now"
                    }
                    i {
                        + "If you already installed the app on this device, please select it when opening the link"
                    }
                }
            }
        }
    }
}