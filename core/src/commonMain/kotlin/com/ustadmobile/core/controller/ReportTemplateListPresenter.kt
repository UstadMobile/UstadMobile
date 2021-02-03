package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ReportDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.ReportTemplateListView
import com.ustadmobile.core.view.SelectionOption
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class ReportTemplateListPresenter(context: Any, arguments: Map<String, String>, view: ReportTemplateListView,
                                  di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ReportTemplateListView, Report>(context, arguments, view, di, lifecycleOwner) {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    private fun updateListOnView() {
        view.list = repo.reportDao.findAllActiveReport("".toQueryLikeParam(),
                0, ReportDao.SORT_TITLE_ASC,
                true)
    }

    override fun handleClickEntry(entry: Report){
        entry.reportUid = 0
        entry.isTemplate = false

        systemImpl.go(ReportEditView.VIEW_NAME,
                mapOf(UstadEditView.ARG_ENTITY_JSON to
                        safeStringify(di, Report.serializer(),
                                entry)),
                context)
    }

    override suspend fun onCheckListSelectionOptions(account: UmAccount?): List<SelectionOption> {
        return listOf(SelectionOption.HIDE)
    }

    override fun handleClickSelectionOption(selectedItem: List<Report>, option: SelectionOption) {
        GlobalScope.launch(doorMainDispatcher()) {
            when (option) {
                SelectionOption.HIDE -> {
                    repo.reportDao.toggleVisibilityReportItems(true,
                            selectedItem.map { it.reportUid }
                                    .filter { it != Report.TEMPLATE_BLANK_REPORT_UID })
                    view.showSnackBar(systemImpl.getString(MessageID.action_hidden, context),
                            { GlobalScope.launch(doorMainDispatcher()){
                                repo.reportDao.toggleVisibilityReportItems(false,
                                        selectedItem.map { it.reportUid })
                            }
                            }, MessageID.content_editor_menu_undo)
                }
            }
        }
    }

    override fun handleClickCreateNewFab() {

    }


}