package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzLogListAttendancePresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignCenterItems
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.formatFullDate
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umItemThumbnail
import kotlinx.css.*
import mui.material.AvatarVariant
import mui.material.ChipColor
import mui.material.styles.TypographyVariant
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class ClazzLogListAttendanceComponent (mProps: UmProps) : UstadListComponent<ClazzLog, ClazzLog>(mProps),
    ClazzLogListAttendanceView {

    private var mPresenter: ClazzLogListAttendancePresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzLogDao

    override val listPresenter: UstadListPresenter<*, in ClazzLog>?
        get() = mPresenter

    var selectedFilter = VIEW_ID_TO_NUMDAYS_MAP.entries.toList().first().key

    private var graphRawData: Array<Array<Any>> = arrayOf()

    override var clazzTimeZone: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    private val graphDataObserver = ObserverFnWrapper<ClazzLogListAttendancePresenter.AttendanceGraphData> {

    }

    override var graphData: MutableLiveData<ClazzLogListAttendancePresenter.AttendanceGraphData>? =
        null
        set(value) {
            field?.removeObserver(graphDataObserver)
            field = value
            field?.observe(this, graphDataObserver)
        }

    override var recordAttendanceOptions: List<ClazzLogListAttendancePresenter.RecordAttendanceOption>? =
        null
        set(value) {
            field = value
            updateUiWithStateChangeDelay {
                fabManager?.visible = !value.isNullOrEmpty()
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        fabManager?.text = getString(MessageID.record_attendance)
        fabManager?.icon = "assignment_turned_in"
        mPresenter = ClazzLogListAttendancePresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }


    override fun RBuilder.renderListHeaderView() {
        umGridContainer(GridSpacing.spacing4) {
            if(graphRawData.isNotEmpty()){
                umItem(GridSize.cells12){
                    umChart(graphRawData){}
                }
            }

            umItem(GridSize.cells12, flexDirection = FlexDirection.row) {
                css {
                    +alignCenterItems
                    padding(2.spacingUnits)
                }

                for (entry in VIEW_ID_TO_NUMDAYS_MAP.entries) {
                    umChip(getString(entry.key),
                        color = if (entry.key == selectedFilter) ChipColor.primary else ChipColor.default,
                        onClick = {
                            setState {
                                selectedFilter = entry.key
                            }
                            mPresenter?.handleClickGraphDuration(entry.value)
                        }) {
                        css {
                            margin(left = 1.spacingUnits, right = 1.spacingUnits)
                        }
                    }
                }
            }
        }
    }

    override fun RBuilder.renderListItem(item: ClazzLog) {

        val total =
            (item.clazzLogNumPresent + item.clazzLogNumPartial + item.clazzLogNumAbsent).toDouble()

        umGridContainer(GridSpacing.spacing4) {
            umItem(GridSize.cells3, GridSize.cells1) {
                umItemThumbnail(
                    "calendar_today",
                    avatarVariant = AvatarVariant.circular
                )
            }

            umItem(GridSize.cells9, GridSize.cells11) {
                umGridContainer {
                    umItem(GridSize.cells12) {
                        umTypography(Date(item.logDate).formatFullDate(clazzTimeZone),
                            variant = TypographyVariant.body1
                        ) {
                            css(StyleManager.alignTextToStart)
                        }
                    }

                    umItem(GridSize.cells12) {
                        css {
                            display = Display.flex
                            marginTop = 5.px
                            marginBottom = 5.px
                            flexDirection = FlexDirection.row
                        }

                        createAttendanceIndicator(item.clazzLogNumPresent / total, Color.green)

                        createAttendanceIndicator(item.clazzLogNumPartial / total, Color.orange)

                        createAttendanceIndicator(item.clazzLogNumAbsent / total, Color.red)

                    }

                    umItem(GridSize.cells12) {
                        umTypography(makeStatus(item), variant = TypographyVariant.body2) {
                            css(StyleManager.alignTextToStart)
                        }
                    }
                }
            }
        }
    }

    override fun handleClickEntry(entry: ClazzLog) {
        mPresenter?.handleClickEntry(entry)
    }

    override fun RBuilder.renderAddContentOptionsDialog() {
        if (showAddEntryOptions) {
            val options = recordAttendanceOptions?.map { clazzLogAttOption ->
                val optionCode = if (clazzLogAttOption.messageId == MessageID.add_a_new_occurrence)
                    2 else 1
                val recordNew = clazzLogAttOption.messageId == MessageID.add_a_new_occurrence
                val option = ClazzLogListAttendancePresenter.RecordAttendanceOption.values().first {
                    it.commandId == optionCode
                }

                UmDialogOptionItem(if (recordNew) "add" else "calendar_today",
                    clazzLogAttOption.messageId,
                    onOptionItemClicked = {
                        mPresenter?.handleClickRecordAttendance(option)
                    })
            } ?: listOf()

            renderDialogOptions(systemImpl, options) {
                setState {
                    showAddEntryOptions = false
                }
            }
        }
    }

    override fun onFabClicked() {
        setState {
            showAddEntryOptions = true
        }
    }

    private fun makeStatus(item: ClazzLog): String {
        return when (item.clazzLogStatusFlag) {
            ClazzLog.STATUS_CREATED -> getString(MessageID.not_recorded)
            ClazzLog.STATUS_HOLIDAY -> "${getString(MessageID.holiday)} - ${item.cancellationNote}"
            ClazzLog.STATUS_RECORDED -> getString(MessageID.present_late_absent).format(
                item.clazzLogNumPresent, item.clazzLogNumPartial, item.clazzLogNumAbsent
            )
            else -> ""
        }

    }

    private fun RBuilder.createAttendanceIndicator(attendance: Double, color: Color) {
        styledDiv {
            css {
                backgroundColor = color
                height = LinearDimension("4px")
                width = LinearDimension("${attendance * 100}%")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }

    companion object {
        val VIEW_ID_TO_NUMDAYS_MAP = mapOf(
            MessageID.last_week to 7,
            MessageID.last_month to 30,
            MessageID.last_three_months to 90
        )
    }
}