package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzAssignmentListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.calculateScoreWithPenalty
import com.ustadmobile.core.view.ClazzAssignmentListView
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.isRealDate
import com.ustadmobile.util.ext.standardFormat
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umItemThumbnail
import kotlinx.css.FlexDirection
import kotlinx.css.padding
import react.RBuilder
import react.setState
import styled.css
import styled.styledSpan

class ClazzAssignmentListComponent(mProps: UmProps): UstadListComponent<ClazzAssignmentWithMetrics, ClazzAssignmentWithMetrics>(mProps),
    ClazzAssignmentListView {

    private var mPresenter: ClazzAssignmentListPresenter? = null

    override val viewNames: List<String>
        get() = listOf(ClazzAssignmentListView.VIEW_NAME)

    override val listPresenter: UstadListPresenter<*, in ClazzAssignmentWithMetrics>?
        get() = mPresenter

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzAssignmentDao

    override fun handleClickEntry(entry: ClazzAssignmentWithMetrics) {
        mPresenter?.onClickAssignment(entry)
    }

    override var clazzTimeZone: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.text = getString(MessageID.clazz_assignment)
        mPresenter = ClazzAssignmentListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: ClazzAssignmentWithMetrics) {
        umGridContainer {
            css{
                padding(top = 1.spacingUnits, bottom = 1.spacingUnits)
            }

            umItem(GridSize.cells3, GridSize.cells1) {
                umItemThumbnail("assignment", avatarVariant = AvatarVariant.circle, width = 60)
            }

            umItem(GridSize.cells9, GridSize.cells11) {
                umGridContainer(rowSpacing = GridSpacing.spacing1) {
                    umItem(GridSize.cells12) {
                        umTypography(item.caTitle, variant = TypographyVariant.h6) {
                            css(alignTextToStart)
                        }
                    }

                    if(!item.caDescription.isNullOrEmpty()){
                        umItem(GridSize.cells12) {
                            umTypography(item.caDescription, variant = TypographyVariant.body2) {
                                css{
                                    +alignTextToStart
                                    padding(bottom = 1.spacingUnits)
                                }
                            }
                        }
                    }

                    umGridContainer(rowSpacing =  GridSpacing.spacing1) {

                        if(item.caDeadlineDate.isRealDate()){
                            umItem(GridSize.cells12, GridSize.cells4, flexDirection = FlexDirection.row){
                                styledSpan {
                                    umIcon("calendar_today")
                                    css{
                                        padding(right = 2.spacingUnits)
                                    }
                                }
                                umTypography(item.caDeadlineDate.toDate()?.standardFormat(),
                                    variant = TypographyVariant.body2) {
                                    css(alignTextToStart)
                                }
                            }
                        }


                        if(item.progressSummary?.hasMetricsPermission == true && item.studentScore != null){
                            umItem(GridSize.cells12, GridSize.cells4, flexDirection = FlexDirection.row){

                                styledSpan {
                                    umIcon("emoji_events")
                                    css{
                                        padding(right = 2.spacingUnits)
                                    }
                                }

                                umTypography("${item.studentScore?.calculateScoreWithPenalty()}%",
                                    variant = TypographyVariant.body2) {
                                    css(alignTextToStart)
                                }

                                styledSpan {
                                    umTypography("(${item.studentScore?.resultScore} / ${item.studentScore?.resultMax})",
                                        variant = TypographyVariant.body2) {
                                        css(alignTextToStart)
                                    }
                                    css{
                                        padding(left = 2.spacingUnits)
                                    }
                                }
                            }
                        }
                    }

                    umItem(GridSize.cells12){
                        umTypography(getString(MessageID.three_num_items_with_name_with_comma).format(
                            item.progressSummary?.notStartedStudents ?: "",
                            getString(MessageID.not_started),
                            item.progressSummary?.calculateStartedStudents() ?: "",
                            getString(MessageID.started),
                            item.progressSummary?.completedStudents.toString(),
                            getString(MessageID.completed)
                        ),
                            variant = TypographyVariant.body2) {
                            css(alignTextToStart)
                        }
                    }

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
    }
}