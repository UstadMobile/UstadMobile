package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressOverviewListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.calculateScoreWithPenalty
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.formatToStringHoursMinutesSeconds
import com.ustadmobile.util.ext.standardFormat
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.createSummaryCard
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umProfileAvatar
import kotlinx.css.*
import react.RBuilder
import react.setState
import styled.css
import styled.styledSpan

class ClazzAssignmentDetailStudentProgressListOverviewComponent (props: UmProps):
    UstadListComponent<PersonWithAttemptsSummary, PersonWithAttemptsSummary>(props),
    ClazzAssignmentDetailStudentProgressOverviewListView {

    private var mPresenter: ClazzAssignmentDetailStudentProgressOverviewListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzDao

    override val viewNames: List<String>
        get() = listOf(ClazzAssignmentDetailStudentProgressOverviewListView.VIEW_NAME)

    override val listPresenter: UstadListPresenter<*, in PersonWithAttemptsSummary>?
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
        useCards = false
        multiColumnItemSize = GridSize.cells6
        mPresenter = ClazzAssignmentDetailStudentProgressOverviewListPresenter(this, arguments, this,di,this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderHeaderView() {
        umGridContainer(GridSpacing.spacing4) {
            createSummaryCard(summary?.notStartedStudents, getString(MessageID.not_started))
            createSummaryCard(summary?.startedStudents, getString(MessageID.started))
            createSummaryCard(summary?.completedStudents, getString(MessageID.completed))
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

    override fun RBuilder.renderListItem(item: PersonWithAttemptsSummary) {
        umGridContainer(GridSpacing.spacing4) {
            val padding = LinearDimension("4px")
            css{
                padding(top = padding, bottom = padding)
            }

            umItem(GridSize.cells2, GridSize.cells2){
                umProfileAvatar(item.personUid, "person")
            }

            umItem(GridSize.cells9, GridSize.cells10){
                umGridContainer {
                    umItem(GridSize.cells12){
                        umTypography("${item.firstNames} ${item.lastName}",
                            variant = TypographyVariant.h6){
                            css (StyleManager.alignTextToStart)
                        }
                    }

                    umItem(GridSize.cells12, flexDirection = FlexDirection.row){

                        styledSpan {
                            css{
                                padding(right = 4.spacingUnits)
                            }
                            umTypography("${item.attempts} ${systemImpl.getString(MessageID.attempts, this)}",
                                variant = TypographyVariant.body1,
                                paragraph = true){
                                css(StyleManager.alignTextToStart)
                            }
                        }

                        if(item.duration > 60000){

                            styledSpan {
                                css{
                                    padding(right = 2.spacingUnits)
                                }
                                umIcon("timer", fontSize = IconFontSize.small){
                                    css{
                                        marginTop = 1.px
                                    }
                                }
                            }

                            styledSpan {
                                css{
                                    padding(right = 2.spacingUnits)
                                }

                                umTypography(item.duration.formatToStringHoursMinutesSeconds(systemImpl),
                                    variant = TypographyVariant.body1,
                                    paragraph = true){
                                    css(StyleManager.alignTextToStart)
                                }
                            }
                        }

                    }

                    if(item.startDate > 0L){
                        umItem (GridSize.cells12){
                            val endDate = if(item.endDate == 0L) "" else " - ${item.endDate.toDate()?.standardFormat()}"
                            umTypography("${item.startDate.toDate()?.standardFormat()}$endDate",
                                variant = TypographyVariant.body1){
                                css (StyleManager.alignTextToStart)
                            }
                        }
                    }

                    if(item.scoreProgress?.progress ?: 0 > 0){
                        umItem (GridSize.cells12, flexDirection = FlexDirection.row){
                            umLinearProgress(item.scoreProgress?.progress?.toDouble(),
                                variant = ProgressVariant.determinate){
                                css (StyleManager.studentProgressBar)
                            }

                            styledSpan {
                                css{
                                    padding(left = 4.spacingUnits)
                                }
                                umTypography(systemImpl.getString(MessageID.percentage_complete, this)
                                    .format(item.scoreProgress?.progress ?: 0),
                                    variant = TypographyVariant.body1,
                                    paragraph = true){
                                    css(StyleManager.alignTextToStart)
                                }
                            }
                        }
                    }

                    if(item.scoreProgress?.resultMax ?: 0 > 0){
                        umItem (GridSize.cells12, flexDirection = FlexDirection.row){
                            umLinearProgress(item.scoreProgress?.resultMax?.toDouble(),
                                variant = ProgressVariant.determinate){
                                css (StyleManager.studentProgressBar)
                            }
                            styledSpan {
                                css {
                                    padding(left = 4.spacingUnits)
                                }

                                umTypography(
                                    systemImpl.getString(MessageID.percentage_score, this)
                                        .format(item.scoreProgress?.calculateScoreWithPenalty() ?: 0),
                                    variant = TypographyVariant.body1,
                                    paragraph = true
                                ) {
                                    css(StyleManager.alignTextToStart)
                                }
                            }

                        }
                    }

                    if(!item.latestPrivateComment.isNullOrEmpty()){
                        umItem(GridSize.cells12, flexDirection = FlexDirection.row){
                            styledSpan {
                                css {
                                    padding(right = 4.spacingUnits)
                                }
                                umIcon("comment", fontSize = IconFontSize.small){
                                    css{
                                        marginTop = 1.px
                                    }
                                }
                            }

                            umTypography(item.latestPrivateComment,
                                variant = TypographyVariant.body2,
                                paragraph = true){
                                css(StyleManager.alignTextToStart)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun handleClickEntry(entry: PersonWithAttemptsSummary) {
        mPresenter?.onClickPersonWithStatementDisplay(entry)
    }

    override fun onFabClicked() {}

    override fun RBuilder.renderAddContentOptionsDialog() {}

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}