package com.ustadmobile.core.controller

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.enrolPersonIntoSchoolAtLocalTimezone
import com.ustadmobile.core.view.JoinWithCodeView
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.SchoolMember
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
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

        val apiUrl = arguments.get(UstadView.ARG_SERVER_URL)?:""
        val tableId = arguments.get(UstadView.ARG_CODE_TABLE)
        val code = arguments.get(UstadView.ARG_CODE) ?:""
        entityTableId = tableId?.toInt()?:0
        val loggedInPersonUid = accountManager.activeAccount.personUid

        var validEntity = when (entityTableId) {
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
        if(apiUrl.isNotEmpty() && validEntity &&
                accountManager.activeAccount.endpointUrl.equals(apiUrl)
                && loggedInPersonUid != 0L) {
            //Continue..

        }
        else if(apiUrl.isNotEmpty() && !accountManager.activeAccount.endpointUrl.equals(apiUrl)){

            //Ignoring entity check and proceeding to login
            //Go to login
            systemImpl.go(Login2View.VIEW_NAME, mapOf(UstadView.ARG_SERVER_URL to apiUrl,
                    UstadView.ARG_NEXT to
                            "${JoinWithCodeView.VIEW_NAME}?${UstadView.ARG_SERVER_URL}=$apiUrl" +
                            "&${UstadView.ARG_CODE_TABLE}=$tableId"),
                    context)
        }
        else if (!validEntity) {
            //Send message invalid code.
            view.errorText = systemImpl.getString(MessageID.invalid_register_code,
                    context)
        }else{
            //Go to login
            systemImpl.go(Login2View.VIEW_NAME, mapOf(UstadView.ARG_SERVER_URL to apiUrl,
                UstadView.ARG_NEXT to
                    "${JoinWithCodeView.VIEW_NAME}?${UstadView.ARG_SERVER_URL}=$apiUrl" +
                        "&${UstadView.ARG_CODE_TABLE}=$tableId"),
                context)
        }
    }

    fun handleClickDone(code: String) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(entityTableId == Clazz.TABLE_ID){
                val clazzToJoin = dbRepo.clazzDao.findByClazzCode(code)
                val personToEnrol = dbRepo.takeIf { clazzToJoin != null }?.personDao
                        ?.findByUid(accountManager.activeAccount.personUid)
                if(clazzToJoin  != null && personToEnrol != null) {
                    dbRepo.enrolPersonIntoClazzAtLocalTimezone(personToEnrol,
                            clazzToJoin.clazzUid, ClazzMember.ROLE_STUDENT_PENDING)
                    view.finish()
                }else {
                    view.errorText = systemImpl.getString(MessageID.invalid_register_code,
                            context)
                }
            }else if(entityTableId == School.TABLE_ID){
                val schoolToJoin = dbRepo.schoolDao.findBySchoolCode(code)
                val personToEnrol = dbRepo.takeIf { schoolToJoin != null }?.personDao
                        ?.findByUid(accountManager.activeAccount.personUid)
                if(schoolToJoin  != null && personToEnrol != null) {
                    dbRepo.enrolPersonIntoSchoolAtLocalTimezone(personToEnrol,
                            schoolToJoin.schoolUid, SchoolMember.Companion.SCHOOL_ROLE_STUDENT_PENDING)
                    view.finish()
                }else {
                    view.errorText = systemImpl.getString(MessageID.invalid_register_code,
                            context)
                }
            }

        }
    }

}