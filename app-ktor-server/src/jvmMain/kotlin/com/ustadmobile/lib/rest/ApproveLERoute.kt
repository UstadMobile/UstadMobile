package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.ext.DoorTag
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on


fun Route.ApproveLERoute() {
    get("/approvele"){
        val config = this.context.application.environment.config
        val di : DI by di()
        val systemImpl : UstadMobileSystemImpl = di.direct.instance()
        val db: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_REPO)
        val personUid = call.request.queryParameters["personUid"]?.toLong() ?: 0L

        val lePerson = db.personDao.findByUid(personUid)
        val message = if(lePerson != null){
            if(lePerson.active){
                "LE already approved"
            }else {
                lePerson.active = true
                db.personDao.update(lePerson)
                "LE approved"
            }
        }else{
            "Unable to find LE. Please check the url."
        }
        val nextStepMessage = if(lePerson?.active == true){
            "They can now login with their account username: ${lePerson?.username?:""}"
        }else{
            ""
        }

        val descriptionMessage = message  +": Full name: ${lePerson?.fullName()?:"N/A"}, " +
        "Org id: ${lePerson?.personOrgId?:"N/A"}, " + nextStepMessage

        call.respondHtml {
            head {
                title {
                    +message
                }
            }

            body {
                h1 {
                    +message
                }

                p {
                    + descriptionMessage
                    br {  }

                }
            }
        }

    }
}


