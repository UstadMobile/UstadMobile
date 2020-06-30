package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.lib.db.entities.DeviceSession
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.authenticateEncryptedPassword
import com.ustadmobile.lib.util.getSystemTimeInMillis
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

private const val DEFAULT_SESSION_LENGTH = (1000L * 60 * 60 * 24 * 365)//One year

fun Route.LoginRoute(db: UmAppDatabase) {
    route("auth") {
        get("login") {
            val username = call.request.queryParameters["username"]
            val password = call.request.queryParameters["password"]
            val deviceId = call.request.header("X-nid")?.toInt()
            if(username == null || password == null || deviceId == null){
                call.respond(HttpStatusCode.BadRequest, "No username/password provided, or no device id")
                return@get
            }

            val person = db.personDao.findUidAndPasswordHashAsync(username)
            if(person != null
                    && ((person.passwordHash.startsWith(PersonAuthDao.PLAIN_PASS_PREFIX)
                            && person.passwordHash.substring(2) == password)
                        ||(person.passwordHash.startsWith(PersonAuthDao.ENCRYPTED_PASS_PREFIX) &&
                            authenticateEncryptedPassword(password, person.passwordHash.substring(2))))){

                db.deviceSessionDao.insert(DeviceSession(dsDeviceId = deviceId,
                        dsPersonUid = person.personUid, expires = getSystemTimeInMillis() + DEFAULT_SESSION_LENGTH))

                call.respond(HttpStatusCode.OK,
                        UmAccount(person.personUid, username, "", null))
            }else {
                call.respond(HttpStatusCode.Forbidden, "")
            }
        }
    }
}
