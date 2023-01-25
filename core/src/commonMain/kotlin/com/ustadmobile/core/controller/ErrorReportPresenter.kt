package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.getOs
import com.ustadmobile.core.impl.getOsVersion
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.view.ErrorReportView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ErrorReport
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class ErrorReportPresenter(
    context: Any,
    args: Map<String, String>,
    view: ErrorReportView,
    di: DI)
: UstadBaseController<ErrorReportView>(context, args, view, di) {

    private val navController: UstadNavController by instance()

    private val accountManager: UstadAccountManager by instance()

    private val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_DB)

    private val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)

    private val systemImpl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        val savedErrUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong()
            ?: navController.currentBackStackEntry?.savedStateHandle
                ?.get(KEY_ERROR_SAVED)?.toLong()
            ?: 0

        presenterScope.launch(doorMainDispatcher()) {
            val errorReport = if(savedErrUid == 0L) {
                ErrorReport().apply{
                    errorCode = arguments[ErrorReportView.ARG_ERR_CODE]?.toInt() ?: -1
                    message = arguments[ErrorReportView.ARG_MESSAGE]
                    stackTrace = arguments[ErrorReportView.ARG_STACKTRACE_PREFKEY]?.let{ stackTraceKey ->
                        systemImpl.getAppPref(stackTraceKey)?.also {
                            systemImpl.setAppPref(stackTraceKey, null)
                        }
                    }
                    presenterUri = arguments[ErrorReportView.ARG_PRESENTER_URI]

                    timestamp = systemTimeInMillis()
                    appVersion = systemImpl.getVersion(context)
                    operatingSys = getOs()
                    osVersion = getOsVersion()
                    errUid = repo.errorReportDao.insertAsync(this)
                    navController.currentBackStackEntry?.savedStateHandle?.set(KEY_ERROR_SAVED,
                        errUid.toString())
                }
            }else {
                //lookup from db
                db.errorReportDao.findByUidAsync(savedErrUid)
            }

            view.errorReport = errorReport
        }
    }

    fun handleClickTakeMeHome() {
        systemImpl.go(systemImpl.getAppConfigDefaultFirstDest(), mapOf(), context,
            UstadMobileSystemCommon.UstadGoOptions(
                arguments[UstadView.ARG_POPUPTO_ON_FINISH] ?: UstadView.ROOT_DEST,
                false))
    }

    companion object {
        const val KEY_ERROR_SAVED = "errSaved"
    }

}