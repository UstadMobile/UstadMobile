package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.AlreadyEnroledInClassException
import com.ustadmobile.core.util.ext.AlreadyEnroledInSchoolException
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.enrolPersonIntoSchoolAtLocalTimezone
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_SNACK_MESSAGE
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.GlobalScope
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

        val tableId = arguments[UstadView.ARG_CODE_TABLE]?.toIntOrNull()
        val codeArg = arguments[UstadView.ARG_CODE] ?:""
        view.code = codeArg
        view.buttonLabel = if(tableId == Clazz.TABLE_ID) {
            systemImpl.getString(MessageID.join_class, context)
        }else {
            systemImpl.getString(MessageID.join_school, context)
        }

        entityTableId = tableId ?: 0
    }

    override fun onStart() {
        super.onStart()

        //If the code is in the args, we should make only one attempt to use it
        //Because it can lead to navigation, this must be done in onStart
        val codeFromArgsUsed = requireNavController().currentBackStackEntry?.savedStateHandle
            ?.get<String>(CODE_FROM_ARGS_USED)?.toBoolean() ?: false

        if(!codeFromArgsUsed) {
            val codeArg = arguments[UstadView.ARG_CODE] ?:""
            if(codeArg.isNotEmpty()) {
                requireNavController().currentBackStackEntry?.savedStateHandle
                    ?.set(CODE_FROM_ARGS_USED, true.toString())
                handleClickDone(codeArg)
            }else {
                view.loading = false
            }
        }

    }

    fun handleClickDone(code: String) {
        if(code.isEmpty()){
            view.errorText = systemImpl.getString(MessageID.field_required_prompt, context)
            return
        }

        view.loading = true
        presenterScope.launch {
            if(entityTableId == Clazz.TABLE_ID){
                Napier.d { "attempting to join class with code ${code.trim()}"}
                var clazzToJoin = dbRepo.clazzDao.findByClazzCode(code.trim())
                if(clazzToJoin == null) {
                    try {
                        clazzToJoin = dbRepo.clazzDao.findByClazzCodeFromWeb(code.trim())
                        if(clazzToJoin != null)
                            dbRepo.clazzDao.insertAsync(clazzToJoin)
                    }catch(e: Exception) {
                        Napier.e("Could not retrieve class using class code ${code.trim()} by http",
                            e)
                    }
                }

                Napier.d { "JoinWithCode: attempting to join course ${clazzToJoin?.clazzName}"}
                val personToEnrol = dbRepo.takeIf { clazzToJoin != null }?.personDao
                        ?.findByUidAsync(accountManager.activeAccount.personUid)
                try {
                    if(clazzToJoin  != null && personToEnrol != null) {
                        Napier.d { "JoinWithCode: enroling into course "}
                        dbRepo.enrolPersonIntoClazzAtLocalTimezone(personToEnrol,
                            clazzToJoin.clazzUid, ClazzEnrolment.ROLE_STUDENT_PENDING)
                        val message = systemImpl.getString(MessageID.please_wait_for_approval, context)
                        systemImpl.go(ClazzList2View.VIEW_NAME,
                            mapOf(ARG_SNACK_MESSAGE to message), context,
                            UstadMobileSystemCommon.UstadGoOptions(popUpToViewName = UstadView.CURRENT_DEST,
                                popUpToInclusive = true))
                    }else {
                        view.loading = false
                        view.errorText = systemImpl.getString(MessageID.invalid_register_code,
                            context)
                    }
                }catch(e: AlreadyEnroledInClassException) {
                    view.loading = false
                    view.errorText = systemImpl.getString(MessageID.you_are_already_in_class, context)
                        .replace("%1\$s", clazzToJoin?.clazzName ?: "")
                }
            }else if(entityTableId == School.TABLE_ID){
                var schoolToJoin = dbRepo.schoolDao.findBySchoolCode(code.trim())
                if(schoolToJoin == null) {
                    try {
                        schoolToJoin = dbRepo.schoolDao.findBySchoolCodeFromWeb(code.trim())
                        if(schoolToJoin != null)
                            dbRepo.schoolDao.insertAsync(schoolToJoin)
                    }catch(e: Exception) {
                        Napier.w("Could not load school via http for code ${code.trim()}", e)
                    }
                }
                val personToEnrol = dbRepo.takeIf { schoolToJoin != null }?.personDao
                        ?.findByUidAsync(accountManager.activeAccount.personUid)
                try {
                    if(schoolToJoin  != null && personToEnrol != null) {
                        dbRepo.enrolPersonIntoSchoolAtLocalTimezone(personToEnrol,
                            schoolToJoin.schoolUid, Role.ROLE_SCHOOL_STUDENT_PENDING_UID)
                        val message = systemImpl.getString(MessageID.please_wait_for_approval,
                            context)
                        systemImpl.go(SchoolListView.VIEW_NAME,
                            mapOf(ARG_SNACK_MESSAGE to message), context,
                            UstadMobileSystemCommon.UstadGoOptions(popUpToViewName = UstadView.CURRENT_DEST,
                                popUpToInclusive = true))
                    }else {
                        view.loading = false
                        view.errorText = systemImpl.getString(MessageID.invalid_register_code,
                            context)
                    }
                }catch(e: AlreadyEnroledInSchoolException) {
                    view.loading = false
                    view.errorText = systemImpl.getString(MessageID.you_are_already_in_school, context)
                        .replace("%1\$s", schoolToJoin?.schoolName ?: "")
                }

            }else {
                view.loading = false
                view.errorText = systemImpl.getString(MessageID.invalid_register_code, context)
            }

        }
    }

    companion object {

        /**
         * Used to remember if the code from the arguments has already been used
         */
        const val CODE_FROM_ARGS_USED = "codeEntered"

    }

}