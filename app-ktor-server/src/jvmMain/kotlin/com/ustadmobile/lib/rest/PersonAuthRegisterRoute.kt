package com.ustadmobile.lib.rest

import com.soywiz.klock.DateTime
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.RegisterRequest
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.LINK_ENDPOINT_VIEWNAME_DIVIDER
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.schedule.age
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.authenticateEncryptedPassword
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.view.ParentalConsentManagementView
import com.ustadmobile.core.view.UstadView
import io.ktor.request.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.ktor.closestDI
import kotlin.IllegalStateException

fun Route.PersonAuthRegisterRoute() {

    route("auth") {
        post("login") {
            val di: DI by closestDI()
            val db: UmAppDatabase by di.on(call).instance(tag = DoorTag.TAG_DB)
            val repo: UmAppDatabase by di.on(call).instance(tag = DoorTag.TAG_REPO)

            val username = call.request.queryParameters["username"]
            val password = call.request.queryParameters["password"]
            val deviceId = call.request.header("X-nid")?.toInt()
            if(username == null || password == null || deviceId == null){
                call.respond(HttpStatusCode.BadRequest, "No username/password provided, or no device id")
                return@post
            }

            val site: Site = db.siteDao.getSiteAsync() ?: throw IllegalStateException("No site!")
            val authSalt = site.authSalt ?: throw IllegalStateException("No auth salt!")

            val pbkdf2Params: Pbkdf2Params = di.direct.instance()

            val passwordDoubleHashed = password.encryptWithPbkdf2(authSalt, pbkdf2Params)
                .encryptWithPbkdf2(authSalt, pbkdf2Params)

            var authorizedPerson = db.personDao.findByUsernameAndPasswordHash2(username,
                passwordDoubleHashed)

            if(authorizedPerson == null) {
                //try the old way in case this user does not yet have a PersonAuth2 object
                val person = db.personDao.findUidAndPasswordHashAsync(username)
                if(person != null
                    && ((person.passwordHash.startsWith(PersonAuthDao.PLAIN_PASS_PREFIX)
                            && person.passwordHash.substring(2) == password)
                            ||(person.passwordHash.startsWith(PersonAuthDao.ENCRYPTED_PASS_PREFIX) &&
                            authenticateEncryptedPassword(password, person.passwordHash.substring(2))))) {
                    authorizedPerson = db.personDao.findByUid(0L)

                    //Create the auth object
                    repo.personAuth2Dao.insertAsync(PersonAuth2().apply {
                        pauthUid = person.personUid
                        pauthMechanism = PersonAuth2.AUTH_MECH_PBKDF2_DOUBLE
                        pauthAuth = password.encryptWithPbkdf2(authSalt, pbkdf2Params)
                            .encryptWithPbkdf2(authSalt, pbkdf2Params)
                    })
                }
            }

            if(authorizedPerson != null) {
                call.respond(HttpStatusCode.OK, authorizedPerson.toUmAccount(""))
            }else {
                call.respond(HttpStatusCode.Forbidden, "")
            }
        }

        post("register"){
            val di: DI by closestDI()
            val db: UmAppDatabase by di.on(call).instance(tag = DoorTag.TAG_DB)
            val repo: UmAppDatabase by di.on(call).instance(tag = DoorTag.TAG_REPO)

            val registerRequest: RegisterRequest = call.receive()

            val mPerson = registerRequest.person
            val newPassword = registerRequest.person.newPassword
                ?: throw IllegalArgumentException("register request with no password!")

            val mLangCode = call.request.queryParameters["locale"] ?: "en"

            val mParentContact = registerRequest.parent?.ppjEmail

            //Check to make sure if the person being registered is a minor that there is a parental contact
            if(DateTime(mPerson.dateOfBirth).age() < UstadMobileConstants.MINOR_AGE_THRESHOLD
                && (mParentContact == null)) {
                call.respond(HttpStatusCode.BadRequest,
                    "Person registering is minor and no parental contact provided or no endpoint for link")
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

            if(DateTime(mPerson.dateOfBirth).age() < UstadMobileConstants.MINOR_AGE_THRESHOLD) {
                val mParentJoinVal = registerRequest.parent ?: throw IllegalStateException("Minor without parent join!")
                val mParentContactVal = mParentContact ?: throw IllegalStateException("Minor without parent contact")

                mParentJoinVal.ppjMinorPersonUid = mPerson.personUid
                mParentJoinVal.ppjUid = repo.personParentJoinDao.insertAsync(mParentJoinVal)

                val systemImpl: UstadMobileSystemImpl by closestDI().instance()
                val appName = systemImpl.getString(mLangCode, MessageID.app_name, Any())
                val linkArgs : Map<String, String> = mapOf(UstadView.ARG_ENTITY_UID to
                        mParentJoinVal.ppjUid.toString())
                val linkUrl = (UMFileUtil.joinPaths(registerRequest.endpointUrl,
                    LINK_ENDPOINT_VIEWNAME_DIVIDER) + ParentalConsentManagementView.VIEW_NAME)
                    .appendQueryArgs(linkArgs.toQueryString())

                val emailText = systemImpl.getString(mLangCode, MessageID.parent_child_register_message, Any())
                    .replace("%1\$s", mPerson.fullName())
                    .replace("%2\$s", appName)
                    .replace("%3\$s", linkUrl)
                val subjectText = systemImpl.getString(mLangCode,
                    MessageID.parent_child_register_message_subject, Any())
                    .replace("%1\$s", appName)

                val notificationSender: NotificationSender by closestDI().instance()
                notificationSender.sendEmail(mParentContactVal, subjectText, emailText)
            }

            val authParams: Pbkdf2Params = di.direct.instance()

            repo.insertPersonAuthCredentials2(mPerson.personUid, newPassword, authParams)

            call.respond(HttpStatusCode.OK, mPerson.toUmAccount(""))
        }
    }

    route("password") {
        post("change") {
            val db: UmAppDatabase by closestDI().on(call).instance(tag = DoorTag.TAG_DB)
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
