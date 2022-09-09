package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzLogEditAttendancePresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzLogEditAttendanceView
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignCenterItems
import com.ustadmobile.util.StyleManager.alignEndItems
import com.ustadmobile.util.StyleManager.alignStartItems
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.tabsContainer
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.Util.stopEventPropagation
import com.ustadmobile.util.ext.DATE_FORMAT_DD_MMM_YYYY_HM
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.formatDate
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.*
import kotlinx.css.margin
import mui.material.Size
import mui.material.ToggleButtonColor
import mui.material.styles.TypographyVariant
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ClazzLogEditAttendanceComponent (mProps: UmProps): UstadEditComponent<ClazzLog>(mProps),
    ClazzLogEditAttendanceView {

    private var mPresenter: ClazzLogEditAttendancePresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzLog>?
        get() = mPresenter

    private var disablePrevBtn = false

    private var disableNextBtn = false

    private var activeLogIndex = 0

    var recordWithPersonList: List<ClazzLogAttendanceRecordWithPerson> = listOf()

    private val clazzLogAttendanceRecordListObserver = ObserverFnWrapper<List<ClazzLogAttendanceRecordWithPerson>> {
        setState {
            recordWithPersonList = it
        }
    }

    override var clazzLogAttendanceRecordList: MutableLiveData<List<ClazzLogAttendanceRecordWithPerson>>? = null
        set(value) {
            field?.removeObserver(clazzLogAttendanceRecordListObserver)
            field = value
            value?.observe(this, clazzLogAttendanceRecordListObserver)
        }

    override var clazzLogTimezone: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var clazzLogsList: List<ClazzLog>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    private fun handleOnDateChange(next: Boolean){
        val oldIndex = activeLogIndex
        activeLogIndex = if(activeLogIndex == clazzLogsList?.lastIndex && next)
            clazzLogsList?.lastIndex ?: 0
        else if(activeLogIndex == 0 && !next) 0
        else  (activeLogIndex + if(next) 1 else -1)
        clazzLogsList?.let {
            mPresenter?.handleSelectClazzLog(it[oldIndex], it[activeLogIndex])
        }
    }

    override var entity: ClazzLog? = null
        get() = field
        set(value) {
            setState {
                value?.let {
                    activeLogIndex = clazzLogsList?.indexOf(value) ?: 0
                }
                field = value
                disableNextBtn = activeLogIndex  >= clazzLogsList?.lastIndex ?: 0
                disablePrevBtn = activeLogIndex == 0
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.attendance)
        mPresenter = ClazzLogEditAttendancePresenter(this, arguments, this,
            di, this)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {

        if(entity == null || clazzLogsList.isNullOrEmpty()) return

        styledDiv {
            css{
                +contentContainer
                +defaultPaddingTop
            }

           umGridContainer {
               umGridContainer {
                   umItem(GridSize.cells1) {
                       css(alignStartItems)
                       umToggleButton(0, Size.medium,
                           disabled = disablePrevBtn){
                           attrs.onClick = { evt, _ ->
                               stopEventPropagation(evt)
                               handleOnDateChange(false)
                           }
                           umIcon("arrow_back_ios")
                       }
                   }

                   umItem(GridSize.cells10) {
                       css{
                           +alignCenterItems
                       }
                       umTypography(entity?.logDate.toDate()?.formatDate(DATE_FORMAT_DD_MMM_YYYY_HM,clazzLogTimezone),
                           variant = TypographyVariant.h6){
                           css{
                               margin(top = 1.spacingUnits)
                           }
                       }
                   }

                   umItem(GridSize.cells1) {
                       css(alignEndItems)

                       umToggleButton(0, Size.medium,
                           disabled = disableNextBtn){
                           attrs.onClick = { evt, _ ->
                               stopEventPropagation(evt)
                               handleOnDateChange(true)
                           }
                           umIcon("arrow_forward_ios")
                       }
                   }
               }

               umItem(GridSize.cells12){
                   css(defaultMarginTop)

                   umGridContainer {
                       css(tabsContainer)
                       umItem(GridSize.cells12){
                           umListItem(button = true){
                               attrs.onClick = {
                                   stopEventPropagation(it)
                                   mPresenter?.handleClickMarkAll(ClazzLogAttendanceRecord.STATUS_ATTENDED)
                               }
                               renderCreateNewItemOnList(getString(MessageID.mark_all_present), "library_add_check")
                           }

                           umListItem(button = true){
                               attrs.onClick = {
                                   stopEventPropagation(it)
                                   mPresenter?.handleClickMarkAll(ClazzLogAttendanceRecord.STATUS_ABSENT)
                               }
                               renderCreateNewItemOnList(getString(MessageID.mark_all_absent), "content_copy")
                           }
                           umSpacer()
                           recordWithPersonList.forEachIndexed { index,record ->
                               umListItem(button = true) {
                                   umGridContainer {
                                       umItem(GridSize.cells3, GridSize.cells1){
                                           umProfileAvatar(record.person?.personUid ?: 0, "person")
                                       }

                                       umItem(GridSize.cells6, GridSize.cells8) {
                                           umTypography(record.person?.fullName() ?: "",
                                               variant = TypographyVariant.h6){
                                               css (StyleManager.alignTextToStart)
                                           }
                                       }

                                       umItem(GridSize.cells3) {
                                          umToggleButtonGroup(size = if(Util.isMobile())Size.small
                                              else Size.medium,
                                              onChange = {
                                                  val selected = it.unsafeCast<Array<Int>>().first()
                                                  setState {
                                                      recordWithPersonList[index].attendanceStatus =
                                                          if(record.attendanceStatus == selected) 0 else selected
                                                  }
                                          }){
                                              val selectedPresent = ClazzLogAttendanceRecord.STATUS_ATTENDED == record.attendanceStatus
                                              val selectedAbsent = ClazzLogAttendanceRecord.STATUS_ABSENT == record.attendanceStatus
                                              val selectedPartial = ClazzLogAttendanceRecord.STATUS_PARTIAL == record.attendanceStatus
                                              umToggleButton(ClazzLogAttendanceRecord.STATUS_ATTENDED,
                                                  selected = selectedPresent,
                                                  color = setSelectedColor(selectedPresent)){
                                                  umIcon("check")
                                              }
                                              umToggleButton(ClazzLogAttendanceRecord.STATUS_ABSENT,
                                                  selected = selectedAbsent,
                                                  color = setSelectedColor(selectedAbsent)){
                                                  umIcon("close")
                                              }
                                              umToggleButton(ClazzLogAttendanceRecord.STATUS_PARTIAL,
                                                  selected = selectedPartial,
                                                  color = setSelectedColor(selectedPartial)){
                                                  umIcon("query_builder")
                                              }
                                          }
                                       }
                                   }
                               }
                           }
                       }
                   }
               }
           }
        }

    }

    private fun setSelectedColor(selected: Boolean): ToggleButtonColor {
        return if(selected) ToggleButtonColor.primary else ToggleButtonColor.standard
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }

}