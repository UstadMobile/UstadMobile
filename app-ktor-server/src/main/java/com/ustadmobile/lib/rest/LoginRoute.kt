package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.lib.db.entities.AccessToken
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.authenticateEncryptedPassword
import com.ustadmobile.lib.util.getSystemTimeInMillis
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.LoginRoute(db: UmAppDatabase) {
    route("Login") {
        get("login") {
            val username = call.request.queryParameters["username"]
            val password = call.request.queryParameters["password"]
            if(username == null || password == null){
                call.respond(HttpStatusCode.BadRequest, "No username/password provided")
                return@get
            }

            val person = db.personDao.findUidAndPasswordHashAsync(username)
            if(person != null
                    && ((person.passwordHash.startsWith(PersonAuthDao.PLAIN_PASS_PREFIX)
                            && person.passwordHash.substring(2) == password)
                        ||(person.passwordHash.startsWith(PersonAuthDao.ENCRYPTED_PASS_PREFIX) &&
                            authenticateEncryptedPassword(password, person.passwordHash.substring(2))))){
                val accessToken = AccessToken(person.personUid,
                        getSystemTimeInMillis() + PersonDao.SESSION_LENGTH)
                call.respond(HttpStatusCode.OK,
                        UmAccount(person.personUid, username, accessToken.token, null))
            }else {
                call.respond(HttpStatusCode.Forbidden, "")
            }
        }
    }
}
