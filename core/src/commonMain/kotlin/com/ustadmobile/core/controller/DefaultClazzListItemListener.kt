package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.*

class DefaultClazzListItemListener(var view: ClazzList2View?,
                                   var listViewMode: ListViewMode,
                                   val context: Any,
                                   override val di: DI): ClazzListItemListener, DIAware {

    val systemImpl: UstadMobileSystemImpl by instance()

    val accountManager: UstadAccountManager by instance()

    override fun onClickClazz(clazz: Clazz) {
        if(listViewMode == ListViewMode.BROWSER) {
            val db = on(accountManager.activeAccount).direct.instance<UmAppDatabase>(tag = TAG_DB)
            GlobalScope.launch(doorMainDispatcher()) {
                val canOpen = db.clazzDao.personHasPermissionWithClazz(
                        accountManager.activeAccount.personUid, clazz.clazzUid,
                        Role.PERMISSION_CLAZZ_OPEN)
                if(canOpen) {
                    systemImpl.go(ClazzDetailView.VIEW_NAME,
                            mapOf(UstadView.ARG_ENTITY_UID to clazz.clazzUid.toString()), context)
                }else {
                    view?.showSnackBar(systemImpl.getString(MessageID.please_wait_for_approval, context))
                }
            }
        }else {
            view?.finishWithResult(listOf(clazz))
        }
    }
}