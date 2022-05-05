package com.ustadmobile.view

import com.ustadmobile.core.controller.CourseTerminologyListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.CourseTerminologyListView
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.util.UmProps
import com.ustadmobile.view.ext.renderListItemWithLeftIconTitleAndDescription
import react.RBuilder

class CourseTerminologyListComponent(mProps: UmProps): UstadListComponent<CourseTerminology, CourseTerminology>(mProps) ,
    CourseTerminologyListView {

    private var mPresenter: CourseTerminologyListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.courseTerminologyDao

    override val listPresenter: UstadListPresenter<*, in CourseTerminology>?
        get() = mPresenter

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.select_terminology)
        showCreateNewItem = true
        addNewEntryText = getString(MessageID.add_new_terminology)
        fabManager?.visible = false
        mPresenter = CourseTerminologyListPresenter(this, arguments,
            this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: CourseTerminology) {
        renderListItemWithLeftIconTitleAndDescription("language", item.ctTitle, onMainList = true)
    }

    override fun handleClickEntry(entry: CourseTerminology) {
        mPresenter?.onClickCourseTerminology(entry)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}