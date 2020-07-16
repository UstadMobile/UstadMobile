package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.lib.db.entities.DeviceSession
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.authenticateEncryptedPassword
import com.ustadmobile.lib.util.getSystemTimeInMillis
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.serialization.json.Json

private const val DEFAULT_SESSION_LENGTH = (1000L * 60 * 60 * 24 * 365)//One year

fun Route.PersonAuthRegister(db: UmAppDatabase) {

    route("auth") {
        post("login") {
            val username = call.request.queryParameters["username"]
            val password = call.request.queryParameters["password"]
            val deviceId = call.request.header("X-nid")?.toInt()
            if(username == null || password == null || deviceId == null){
                call.respond(HttpStatusCode.BadRequest, "No username/password provided, or no device id")
                return@post
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
                        UmAccount(person.personUid, username, "", "",person.firstNames,person.lastName))
            }else {
                call.respond(HttpStatusCode.Forbidden, "")
            }
        }

        post("register"){
            val personString = call.request.queryParameters["person"]
            val password = call.request.queryParameters["password"]
            if(personString == null || password == null){
                call.respond(HttpStatusCode.BadRequest, "No password or person information provided")
                return@post
            }

            val mPerson = Json.parse(Person.serializer(),personString)

            val person = db.personDao.findByUsername(mPerson.username)

            if(person != null){
                call.respond(HttpStatusCode.Conflict, "Person already exists, change username")
                return@post
            }

            val pUid = db.personDao.insert(mPerson)
            val personAuth = com.ustadmobile.lib.db.entities.PersonAuth(mPerson.personUid,
                    PersonAuthDao.PLAIN_PASS_PREFIX+password)
            val aUid = db.personAuthDao.insert(personAuth)

            if(pUid != -1L && aUid != -1L){
                call.respond(HttpStatusCode.OK,UmAccount(mPerson.personUid, mPerson.username, "",
                        "",mPerson.firstNames,mPerson.lastName))
            }else{
                call.respond(HttpStatusCode.BadRequest, "Failed to register a person")
            }
        }
    }

    route("password") {
        post("change") {
            val username = call.request.queryParameters["username"]
            val currentPassword = call.request.queryParameters["currentPassword"]
            val newPassword = call.request.queryParameters["newPassword"]

            if(username == null || newPassword == null){
                call.respond(HttpStatusCode.BadRequest, "No user id or new password provide")
                return@post
            }

            val person = db.personDao.findUidAndPasswordHashAsync(username)
            if(person != null){

                if(!person.admin && currentPassword == null){
                    call.respond(HttpStatusCode.Forbidden, "No old password provide")
                    return@post
                }

                if(currentPassword != null && ((person.passwordHash.startsWith(PersonAuthDao.PLAIN_PASS_PREFIX)
                                && person.passwordHash.substring(2) == currentPassword)
                                ||(person.passwordHash.startsWith(PersonAuthDao.ENCRYPTED_PASS_PREFIX) &&
                                authenticateEncryptedPassword(currentPassword, person.passwordHash.substring(2))))){
                    call.respond(HttpStatusCode.Forbidden, "Current password doesn't match, try again")
                    return@post
                }

                val personAuth = com.ustadmobile.lib.db.entities.PersonAuth(person.personUid,
                        PersonAuthDao.PLAIN_PASS_PREFIX+newPassword)
                db.personAuthDao.update(personAuth)

                call.respond(HttpStatusCode.OK,UmAccount(person.personUid, username, "",
                        "",person.firstNames,person.lastName))
            }else {
                call.respond(HttpStatusCode.Forbidden, "")
            }
        }
    }
}
