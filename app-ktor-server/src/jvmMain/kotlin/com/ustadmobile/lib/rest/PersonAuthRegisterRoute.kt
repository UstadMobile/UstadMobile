package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.lib.db.entities.*
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

fun Route.PersonAuthRegisterRoute(db: UmAppDatabase) {

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
            if(personString == null){
                call.respond(HttpStatusCode.BadRequest, "No person information provided")
                return@post
            }

            val mPerson = Json.parse(PersonWithAccount.serializer(),personString)

            val person = if(mPerson.personUid != 0L) db.personDao.findByUid(mPerson.personUid)
            else db.personDao.findByUsername(mPerson.username)

            if(person != null && (mPerson.personUid == 0L ||
                            mPerson.personUid != 0L && mPerson.username == person.username)){
                call.respond(HttpStatusCode.Conflict, "Person already exists, change username")
                return@post
            }

            if(person == null) {
                mPerson.apply {
                    personUid = db.insertPersonAndGroup(mPerson).personUid
//                    personUid = db.personDao.insert(mPerson)
                }
            } else {
                db.personDao.update(mPerson)
            }
            val personAuth = PersonAuth(mPerson.personUid,
                    PersonAuthDao.PLAIN_PASS_PREFIX+mPerson.newPassword)
            val aUid = db.personAuthDao.insert(personAuth)

//            //create PersonGroup
//            val personGroup = PersonGroup().apply {
//                groupPersonUid = mPerson.personUid
//                groupUid = db.personGroupDao.insert(this)
//            }
//
//            db.personGroupMemberDao.insert(PersonGroupMember(mPerson.personUid, personGroup.groupUid))

            if(aUid != -1L){
                val username = mPerson.username
                if(username != null){
                    val createdPerson = db.personAuthDao.findPersonByUsername(username)
                    if(createdPerson != null){
                        call.respond(HttpStatusCode.OK,UmAccount(createdPerson.personUid,
                                createdPerson.username, "", "",
                                createdPerson.firstNames,createdPerson.lastName))
                    }
                }
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

                if(currentPassword != null && ((person.passwordHash.startsWith(PersonAuthDao.PLAIN_PASS_PREFIX)
                                && person.passwordHash.substring(2) != currentPassword)
                                ||(person.passwordHash.startsWith(PersonAuthDao.ENCRYPTED_PASS_PREFIX) &&
                                authenticateEncryptedPassword(currentPassword, person.passwordHash.substring(2))))){
                    call.respond(HttpStatusCode.Forbidden, "Current password doesn't match, try again")
                    return@post
                }

                val personAuth = PersonAuth(person.personUid,
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
