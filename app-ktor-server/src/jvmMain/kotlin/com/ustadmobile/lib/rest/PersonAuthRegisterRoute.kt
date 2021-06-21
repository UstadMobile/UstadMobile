package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.LINK_ENDPOINT_VIEWNAME_DIVIDER
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.schedule.age
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.toQueryString
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
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on
import com.ustadmobile.core.util.ext.toUmAccount
import com.ustadmobile.core.view.ParentalConsentManagementView
import com.ustadmobile.core.view.UstadView
import kotlin.IllegalStateException

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

            val mLangCode = call.request.queryParameters["locale"] ?: "en"

            val mParentContact = mParentJoin?.ppjEmail

            val mEndpointUrl = call.request.queryParameters["endpoint"]

            //Check to make sure if the person being registered is a minor that there is a parental contact
            if(DateTime(mPerson.dateOfBirth).age() < UstadMobileConstants.MINOR_AGE_THRESHOLD
                && (mParentContact == null || mEndpointUrl == null)) {
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
                val mParentJoinVal = mParentJoin ?: throw IllegalStateException("Minor without parent join!")
                val mEndpointVal = mEndpointUrl ?: throw IllegalStateException("Minor without endpoint val")
                val mParentContactVal = mParentContact ?: throw IllegalStateException("Minor without parent contact")

                mParentJoin.ppjMinorPersonUid = mPerson.personUid
                mParentJoinVal.ppjUid = repo.personParentJoinDao.insertAsync(mParentJoinVal)

                val systemImpl: UstadMobileSystemImpl by di().instance()
                val appName = systemImpl.getString(mLangCode, MessageID.app_name, Any())
                val linkArgs : Map<String, String> = mapOf(UstadView.ARG_ENTITY_UID to
                        mParentJoin.ppjUid.toString())
                val linkUrl = (UMFileUtil.joinPaths(mEndpointVal,
                    LINK_ENDPOINT_VIEWNAME_DIVIDER) + ParentalConsentManagementView.VIEW_NAME)
                    .appendQueryArgs(linkArgs.toQueryString())

                val emailText = systemImpl.getString(mLangCode, MessageID.parent_child_register_message, Any())
                    .replace("%1\$s", mPerson.fullName())
                    .replace("%2\$s", appName)
                    .replace("%3\$s", linkUrl)
                val subjectText = systemImpl.getString(mLangCode,
                    MessageID.parent_child_register_message_subject, Any())
                    .replace("%1\$s", appName)

                val notificationSender: NotificationSender by di().instance()
                notificationSender.sendEmail(mParentContactVal, subjectText, emailText)
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
