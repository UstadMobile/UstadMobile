package com.ustadmobile.view

import com.ccfraser.muirwik.components.MGridSize
import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.mTypography
import com.ustadmobile.core.controller.ClazzLogListAttendancePresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.formatDate
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umProfileAvatar
import kotlinx.css.*
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class ClazzLogListComponent (mProps: RProps) : UstadListComponent<ClazzLog, ClazzLog>(mProps),
    ClazzLogListAttendanceView {

    private var mPresenter: ClazzLogListAttendancePresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzLogDao

    override val listPresenter: UstadListPresenter<*, in ClazzLog>?
        get() = mPresenter

    override val viewName: String
        get() = ClazzLogListAttendanceView.VIEW_NAME

    override var clazzTimeZone: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    private val graphDataObserver = ObserverFnWrapper<ClazzLogListAttendancePresenter.AttendanceGraphData>{
        props.asDynamic()?.drawGraph(it)
    }

    override var graphData: DoorMutableLiveData<ClazzLogListAttendancePresenter.AttendanceGraphData>? = null
        set(value) {
            field?.removeObserver(graphDataObserver)
            field = value
            field?.observe(this, graphDataObserver)
        }

    override var recordAttendanceOptions: List<ClazzLogListAttendancePresenter.RecordAttendanceOption>? = null
        set(value) {
            field = value
            fabManager?.visible = !value.isNullOrEmpty()
        }


    override fun onCreate() {
        super.onCreate()
        fabManager?.text = getString(MessageID.record_attendance)
        fabManager?.icon = "assignment_turned_in"
        mPresenter = ClazzLogListAttendancePresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderAddEntryOptionsDialog() {
        if(showAddEntryOptions){
            val options = recordAttendanceOptions?.map { clazzLogAttOption ->
                val optionCode = if(clazzLogAttOption.messageId == MessageID.add_a_new_occurrence) 2 else 1
                val recordNew = clazzLogAttOption.messageId == MessageID.add_a_new_occurrence
                PopUpOptionItem(if(recordNew) "add" else "calendar_today",
                    clazzLogAttOption.messageId,
                    onOptionItemClicked = {
                        mPresenter?.handleClickRecordAttendance(ClazzLogListAttendancePresenter.RecordAttendanceOption.values().first {
                            it.commandId == optionCode
                        })
                    })
            } ?: listOf()
            renderChoices(systemImpl,options){
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
                item.clazzLogNumPresent, item.clazzLogNumPartial, item.clazzLogNumAbsent)
            else -> ""
        }

    }

    private fun RBuilder.createAttendanceIndicator(attendance: Double, color: Color){
        styledDiv {
            css {
                backgroundColor = color
                height = LinearDimension("4px")
                width = LinearDimension("${attendance * 100}%")
            }
        }
    }

    override fun RBuilder.renderHeaderView() {
        //Handle header view as per android
    }

    override fun RBuilder.renderListItem(item: ClazzLog) {

        val total = (item.clazzLogNumPresent + item.clazzLogNumPartial + item.clazzLogNumAbsent).toDouble()

        umGridContainer {
            umItem(MGridSize.cells3){
                umProfileAvatar(-1, "calendar_today")
            }

            umItem(MGridSize.cells9){
                umGridContainer {
                    umItem(MGridSize.cells12){
                        mTypography(
                            Date(item.logDate).formatDate("dddd, MMMM DD h:m A"),
                            variant = MTypographyVariant.body1){
                            css(StyleManager.alignTextToStart)
                        }
                    }

                    umItem(MGridSize.cells12){
                        css{
                            display = Display.flex
                            marginTop = 5.px
                            marginBottom = 5.px
                            flexDirection = FlexDirection.row
                        }

                        createAttendanceIndicator(item.clazzLogNumPresent/total, Color.green)

                        createAttendanceIndicator(item.clazzLogNumPartial/total, Color.orange)

                        createAttendanceIndicator(item.clazzLogNumAbsent/total, Color.red)

                    }

                    umItem(MGridSize.cells12){
                        mTypography(makeStatus(item),variant = MTypographyVariant.body2){
                            css(StyleManager.alignTextToStart)
                        }
                    }
                }
            }
        }
    }

    override fun handleClickEntry(entry: ClazzLog) {

    }
}

fun RBuilder.renderClazzLogList(drawGraph: (Any) -> Unit) = child(ClazzLogListComponent::class){
    attrs.asDynamic().drawGraph = drawGraph
}