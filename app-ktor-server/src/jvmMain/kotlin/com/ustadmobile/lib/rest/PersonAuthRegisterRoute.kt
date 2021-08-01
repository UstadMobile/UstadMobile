package com.ustadmobile.lib.rest

import com.soywiz.klock.DateTime
import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.AuthResult
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.RegisterRequest
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.LINK_ENDPOINT_VIEWNAME_DIVIDER
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.schedule.age
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.view.ParentalConsentManagementView
import com.ustadmobile.core.view.UstadView
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.util.pipeline.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.ktor.closestDI
import kotlin.IllegalStateException

fun Route.personAuthRegisterRoute() {

    route("auth") {
        post("login") {
            val authManager: AuthManager by closestDI().on(call).instance()

            val username = call.request.queryParameters["username"]
            val password = call.request.queryParameters["password"]
            val maxDateOfBirth = call.request.queryParameters["maxDateOfBirth"]?.toLong() ?: 0L

            val deviceId = call.request.header("X-nid")?.toInt()
            if(username == null || password == null || deviceId == null){
                call.respond(HttpStatusCode.BadRequest, "No username/password provided, or no device id")
                return@post
            }


            val authResult = authManager.authenticate(username, password,
                fallbackToOldPersonAuth = true)
            val authorizedPerson = authResult.authenticatedPerson

            if(authResult.success && authorizedPerson != null) {
                if(maxDateOfBirth == 0L || authorizedPerson.dateOfBirth < maxDateOfBirth) {
                    //If not active ie not approved in most cases or removed.
                    if(!authorizedPerson.active){
                        call.respond(HttpStatusCode.Unauthorized, authorizedPerson.toUmAccount(""))
                    }else {
                        call.respond(HttpStatusCode.OK, authorizedPerson.toUmAccount(""))
                    }
                }else {
                    call.respond(HttpStatusCode.Conflict, "")
                }
            }else if(authResult.reason == AuthResult.REASON_NEEDS_CONSENT) {
                call.respond(HttpStatusCode.FailedDependency, "")
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
                    //Only LEs can sign up but their accounts would be inactive.
                    mPerson.personGoldoziType = Person.GOLDOZI_TYPE_LE
                    mPerson.active = false

                    if(mPerson?.personWeGroupUid == 0L){
                        //Create a WE Group for this LE
                        val leWeGroup = PersonGroup().apply {
                            groupName = mPerson?.fullName() + "'s WE group"
                            personGroupFlag = PersonGroup.PERSONGROUP_FLAG_DEFAULT
                            groupUid = repo.personGroupDao.insertAsync(this)
                        }
                        //Assign WE Group to the Logged in LE
                        mPerson?.personWeGroupUid = leWeGroup.groupUid
                    }
                    personUid = repo.insertPersonAndGroup(mPerson).personUid

                    //Send email
                    //1. Get admin email.
                    //2. Get LE user details : username, full name, dob, registration id
                    //3. Create url with /approvele/ endpoint
                    //4. Create email text and subject text.
                    //5. Send the email to admin's email address

                    val systemImpl: UstadMobileSystemImpl by closestDI().instance()
                    val adminUserEmail = db.personDao.findByUsername("admin")?.emailAddr?:""
                    if(adminUserEmail.isNotEmpty()){
                        val linkUrl = UMFileUtil.joinPaths(registerRequest.endpointUrl,
                            "approvele") + "?personUid=" + personUid

                        val subjectText = systemImpl.getString(mLangCode, MessageID.new_le_request, Any())
                            .replace("%1\$s", mPerson.fullName())

                        val emailText = systemImpl.getString(mLangCode,
                                MessageID.approve_le_request_email, Any())
                            .replace("%1\$s", mPerson.fullName())
                            .replace("%2\$s", mPerson.personOrgId?:"")
                            .replace("%3\$s", mPerson.username?:"")
                            .replace("%4\$s", linkUrl)


                        println(emailText)
                        println(subjectText)

                        val notificationSender: NotificationSender by closestDI().instance()
                        notificationSender.sendEmail(adminUserEmail, subjectText, emailText)

                    }else{
                        //Should we do something here?
                    }

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

            call.respond(HttpStatusCode.OK, mPerson)
        }
    }
}
