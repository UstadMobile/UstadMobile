package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.schedule.age
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.door.ext.DoorTag
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
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on
import com.ustadmobile.core.util.ext.toUmAccount

private const val DEFAULT_SESSION_LENGTH = (1000L * 60 * 60 * 24 * 365)//One year

fun Route.PersonAuthRegisterRoute() {

    route("auth") {
        post("login") {
            val db: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_DB)
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

                call.respond(HttpStatusCode.OK, person.toUmAccount(endpointUrl = "",
                    username = username))
            }else {
                call.respond(HttpStatusCode.Forbidden, "")
            }
        }

        post("register"){
            val db: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_DB)
            val repo: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_REPO)
            val gson: Gson by di().instance()

            val mPerson = call.request.queryParameters["person"]?.let {
                gson.fromJson(it, PersonWithAccount::class.java)
            }

            if(mPerson == null) {
                call.respond(HttpStatusCode.BadRequest, "No person information provided")
                return@post
            }

            val mParentJoin = call.request.queryParameters["parent"]?.let {
                gson.fromJson(it, PersonParentJoin::class.java)
            }

            val mParentContact = mParentJoin?.ppjEmail

            if(DateTime(mPerson.dateOfBirth).age() < UstadMobileConstants.MINOR_AGE_THRESHOLD &&
                    mParentContact == null) {
                call.respond(HttpStatusCode.BadRequest,
                    "Person registering is minor and no parental contact provided")
                return@post
            }



            val existingPerson = if(mPerson.personUid != 0L) db.personDao.findByUid(mPerson.personUid)
            else db.personDao.findByUsername(mPerson.username)

            if(existingPerson != null && (mPerson.personUid == 0L ||
                            mPerson.personUid != 0L && mPerson.username == existingPerson.username)){
                call.respond(HttpStatusCode.Conflict, "Person already exists, change username")
                return@post
            }

            if(existingPerson == null) {
                mPerson.apply {
                    personUid = repo.insertPersonAndGroup(mPerson).personUid
                }
            } else {
                repo.personDao.update(mPerson)
            }
            val personAuth = PersonAuth(mPerson.personUid,
                    PersonAuthDao.PLAIN_PASS_PREFIX+mPerson.newPassword)
            val aUid = db.personAuthDao.insert(personAuth)

            if(aUid != -1L){
                val username = mPerson.username
                if(username != null){
                    val createdPerson = db.personAuthDao.findPersonByUsername(username)
                    if(createdPerson != null){
                        call.respond(HttpStatusCode.OK, createdPerson.toUmAccount(""))
                    }
                }
            }else{
                call.respond(HttpStatusCode.BadRequest, "Failed to register a person")
            }
        }
    }

    route("password") {
        post("change") {
            val db: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_DB)
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

                call.respond(HttpStatusCode.OK, person.toUmAccount("", username))
            }else {
                call.respond(HttpStatusCode.Forbidden, "")
            }
        }
    }
}
