package com.ustadmobile.view

import com.ustadmobile.core.controller.LeavingReasonListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.LeavingReasonListView
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.util.UmProps
import com.ustadmobile.view.ext.renderListItemWithIconAndTitle
import react.RBuilder

class LeavingReasonListComponent(props: UmProps): UstadListComponent<LeavingReason, LeavingReason>(props),
    LeavingReasonListView{

    private var mPresenter: LeavingReasonListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.leavingReasonDao

    override val listPresenter: UstadListPresenter<*, in LeavingReason>?
        get() = mPresenter

    override fun onCreateView() {
        super.onCreateView()
        addNewEntryText = getString(MessageID.add_leaving_reason)
        showCreateNewItem = true
        fabManager?.text = getString(MessageID.leaving_reason)
        ustadComponentTitle = getString(MessageID.select_leaving_reason)
        mPresenter = LeavingReasonListPresenter(this, arguments, this,di,this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: LeavingReason) {
        renderListItemWithIconAndTitle("question_mark", item.leavingReasonTitle ?: "")
    }

    override fun handleClickEntry(entry: LeavingReason) {
        mPresenter?.onClickLeavingReason(entry)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

}