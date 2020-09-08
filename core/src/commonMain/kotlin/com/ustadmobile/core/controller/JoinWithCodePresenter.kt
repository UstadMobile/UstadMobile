package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.view.JoinWithCodeView
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzMember
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class JoinWithCodePresenter(context: Any, args: Map<String, String>, view: JoinWithCodeView, di: DI): UstadBaseController<JoinWithCodeView>(context, args, view, di) {

    val accountManager: UstadAccountManager by instance()

    val dbRepo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = TAG_REPO)

    val systemImpl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    fun handleClickDone(code: String) {
        GlobalScope.launch(doorMainDispatcher()) {
            val clazzToJoin = dbRepo.clazzDao.findByClazzCode(code.trim())
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
        }
    }

}