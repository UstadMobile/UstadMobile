package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.enrolPersonIntoSchoolAtLocalTimezone
import com.ustadmobile.core.view.JoinWithCodeView
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class JoinWithCodePresenter(context: Any, args: Map<String, String>, view: JoinWithCodeView, di: DI)
    : UstadBaseController<JoinWithCodeView>(context, args, view, di) {

    val accountManager: UstadAccountManager by instance()

    val dbRepo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = TAG_REPO)

    val systemImpl: UstadMobileSystemImpl by instance()

    private var entityTableId : Int = 0

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        val goOptions = UstadMobileSystemCommon.UstadGoOptions("",
                true)

        var endpointUrl = accountManager.activeAccount.endpointUrl
        if(!endpointUrl.endsWith("/")){
            endpointUrl = "$endpointUrl/"
        }

        var apiUrl = arguments[UstadView.ARG_SERVER_URL] ?:""
        if(apiUrl.isEmpty()){
            apiUrl = accountManager.activeAccount.endpointUrl
        }
        if(!apiUrl.endsWith("/")){
            apiUrl = "$apiUrl/"
        }

        val tableId = arguments[UstadView.ARG_CODE_TABLE]
        val code = arguments[UstadView.ARG_CODE] ?:""
        entityTableId = tableId?.toInt()?:0
        val loggedInPersonUid = accountManager.activeAccount.personUid

        GlobalScope.launch() {
            val validEntity = when (entityTableId) {
                Clazz.TABLE_ID -> {
                    dbRepo.clazzDao.findByClazzCode(code) != null
                }
                School.TABLE_ID -> {
                    dbRepo.schoolDao.findBySchoolCode(code) != null
                }
                else -> {
                    false
                }
            }

            if (apiUrl.isNotEmpty() && validEntity && endpointUrl.equals(apiUrl)
                    && loggedInPersonUid != 0L) {
                view.runOnUiThread(Runnable {
                    view.code = code
                })

                //Continue..

            } else if (apiUrl.isNotEmpty() && !endpointUrl.equals(apiUrl)) {

                //Ignoring entity check and proceeding to login
                //Go to login
                view.runOnUiThread(Runnable {

                    systemImpl.go(Login2View.VIEW_NAME, mapOf(UstadView.ARG_SERVER_URL to apiUrl,
                            PersonEditView.REGISTER_VIA_LINK to "true",
                        UstadView.ARG_NEXT to
                                "${JoinWithCodeView.VIEW_NAME}?${UstadView.ARG_SERVER_URL}=${apiUrl}" +
                                "&${UstadView.ARG_CODE_TABLE}=${tableId}&${UstadView.ARG_CODE}=$code",
                    Login2View.ARG_NO_GUEST to "true"),
                        context, goOptions)
                })
            } else if (!validEntity) {

                view.runOnUiThread(Runnable {
                    //Send message invalid code.
                    view.code = code
                    view.errorText = systemImpl.getString(MessageID.invalid_register_code,
                            context)

                })
            } else {
                //Go to login
                view.runOnUiThread(Runnable {

                    systemImpl.go(Login2View.VIEW_NAME, mapOf(UstadView.ARG_SERVER_URL to apiUrl,
                            PersonEditView.REGISTER_VIA_LINK to "true",
                            UstadView.ARG_NEXT to
                                    "${JoinWithCodeView.VIEW_NAME}?${UstadView.ARG_SERVER_URL}=${apiUrl}" +
                                    "&${UstadView.ARG_CODE_TABLE}=${tableId}&${UstadView.ARG_CODE}=$code",
                            Login2View.ARG_NO_GUEST to "true"),
                            context, goOptions)
                })
            }
        }
    }

    fun handleClickDone(code: String) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(entityTableId == Clazz.TABLE_ID){
                val clazzToJoin = dbRepo.clazzDao.findByClazzCode(code.trim())
                val personToEnrol = dbRepo.takeIf { clazzToJoin != null }?.personDao
                        ?.findByUid(accountManager.activeAccount.personUid)
                if(clazzToJoin  != null && personToEnrol != null) {
                    dbRepo.enrolPersonIntoClazzAtLocalTimezone(personToEnrol,
                            clazzToJoin.clazzUid, ClazzEnrollment.ROLE_STUDENT_PENDING)
                    view.finish()
                }else {
                    view.errorText = systemImpl.getString(MessageID.invalid_register_code,
                            context)
                }
            }else if(entityTableId == School.TABLE_ID){
                val schoolToJoin = dbRepo.schoolDao.findBySchoolCode(code.trim())
                val personToEnrol = dbRepo.takeIf { schoolToJoin != null }?.personDao
                        ?.findByUid(accountManager.activeAccount.personUid)
                if(schoolToJoin  != null && personToEnrol != null) {
                    dbRepo.enrolPersonIntoSchoolAtLocalTimezone(personToEnrol,
                            schoolToJoin.schoolUid, Role.ROLE_SCHOOL_STUDENT_PENDING_UID)
                    view.finish()
                }else {
                    view.errorText = systemImpl.getString(MessageID.invalid_register_code,
                            context)
                }
            }else {
                view.errorText = systemImpl.getString(MessageID.invalid_register_code,
                        context)
            }

        }
    }

}