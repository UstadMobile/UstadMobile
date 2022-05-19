package com.ustadmobile.view

import com.ustadmobile.core.controller.ContentEntryDetailAttemptsListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.view.ContentEntryDetailAttemptsListView
import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary
import com.ustadmobile.util.UmProps
import com.ustadmobile.view.ext.renderPersonWithAttemptProgress
import react.RBuilder


class ContentEntryDetailAttemptsListComponent(props: UmProps): UstadListComponent<PersonWithAttemptsSummary,
        PersonWithAttemptsSummary>(props), ContentEntryDetailAttemptsListView {

    private var mPresenter: ContentEntryDetailAttemptsListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithAttemptsSummary>?
        get() = mPresenter

    override val displayTypeRepo: Any?
        get() = dbRepo?.statementDao

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.visible = false
        mPresenter = ContentEntryDetailAttemptsListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: PersonWithAttemptsSummary) {
        renderPersonWithAttemptProgress(item, systemImpl)
    }

    override fun handleClickEntry(entry: PersonWithAttemptsSummary) {
        mPresenter?.onClickPersonWithStatementDisplay(entry)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}