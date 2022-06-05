package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressOverviewListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.PersonGroupAssignmentSummary
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.mui.components.umDivider
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.view.ext.renderAssignmentSubmittedProgress
import com.ustadmobile.view.ext.renderSummaryCard
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import react.setState
import styled.css

class ClazzAssignmentDetailStudentProgressListOverviewComponent (props: UmProps):
    UstadListComponent<PersonGroupAssignmentSummary, PersonGroupAssignmentSummary>(props),
    ClazzAssignmentDetailStudentProgressOverviewListView {

    private var mPresenter: ClazzAssignmentDetailStudentProgressOverviewListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzDao

    override val listPresenter: UstadListPresenter<*, in PersonGroupAssignmentSummary>?
        get() = mPresenter

    private var summary: AssignmentProgressSummary? = null

    private val progressObserver = ObserverFnWrapper<AssignmentProgressSummary?>{
        setState {
            summary = it
        }
    }

    override var progressSummary: DoorLiveData<AssignmentProgressSummary?>? = null
        set(value) {
            field = value
            value?.removeObserver(progressObserver)
            value?.observe(this, progressObserver)
        }

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.text = getString(MessageID.clazz)
        linearLayout = false
        useCardsOnGridLayout = false
        columnSize = GridSize.cells6
        mPresenter = ClazzAssignmentDetailStudentProgressOverviewListPresenter(this, arguments, this,di,this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListHeaderView() {
        umGridContainer(GridSpacing.spacing4) {
            renderSummaryCard(summary?.calculateNotSubmittedStudents(), getString(MessageID.not_started))
            renderSummaryCard(summary?.submittedStudents, getString(MessageID.submitted))
            renderSummaryCard(summary?.markedStudents, getString(MessageID.marked_cap))
            umItem(GridSize.cells12){
                umDivider {
                    css{
                        +StyleManager.defaultFullWidth
                        +StyleManager.defaultMarginBottom
                    }
                }
            }
        }
    }

    override fun RBuilder.renderListItem(item: PersonGroupAssignmentSummary) {
        renderAssignmentSubmittedProgress(item, systemImpl, false)
    }

    override fun handleClickEntry(entry: PersonGroupAssignmentSummary) {
        mPresenter?.onClickPerson(entry)
    }

    override fun onFabClicked() {}

    override fun RBuilder.renderAddContentOptionsDialog() {}

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}