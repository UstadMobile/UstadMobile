package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.Schedule
import react.RBuilder
import com.ustadmobile.util.*
import react.setState

class ClazzDetailOverviewComponent(mProps: UmProps): UstadDetailComponent<ClazzWithDisplayDetails>(mProps),
    ClazzDetailOverviewView {

    private var mPresenter: ClazzDetailOverviewPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override val viewName: String
        get() = ClazzDetailOverviewView.VIEW_NAME

    private var schedules: List<Schedule>? = null

    private val observer = ObserverFnWrapper<List<Schedule>>{
        setState {
            schedules = it
        }
    }

    override var scheduleList: DoorDataSourceFactory<Int, Schedule>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(observer)
            liveData?.observe(this, observer)
        }

    override var clazzCodeVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: ClazzWithDisplayDetails? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        editButtonMode = EditButtonMode.FAB
        mPresenter = ClazzDetailOverviewPresenter(this, arguments, this,
            di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
      /*  styledDiv {
            css {
                +defaultPaddingTop
                +contentContainer
            }

            umGridContainer {

                umItem(MGridSize.cells12){
                    mTypography(entity?.clazzDesc,
                        variant = MTypographyVariant.body1,
                        color = MTypographyColor.textPrimary,
                        gutterBottom = true){
                        css(alignTextToStart)
                    }

                }

                val numOfStudentTeachers = getString(MessageID.x_teachers_y_students)
                    .format(entity?.numTeachers ?: 0, entity?.numStudents ?: 0)

                createInformation("people", numOfStudentTeachers, getString(MessageID.members))

                createInformation("login", entity?.clazzCode ?: "", getString(MessageID.class_code)){
                    copyToClipboard(entity?.clazzCode ?: ""){
                        showSnackBar(getString(MessageID.copied_to_clipboard))
                    }
                }

                createInformation("school", entity?.clazzSchool?.schoolName)

                val dateTxt = Date(entity?.clazzStartTime ?: 0).standardFormat() +
                        " - ${Date(entity?.clazzEndTime ?: 0).standardFormat()}"

                createInformation("event", dateTxt)

                createInformation("event", entity?.clazzHolidayCalendar?.umCalendarName)


                if(!schedules.isNullOrEmpty()){
                    umItem(MGridSize.cells12){
                        createListSectionTitle(getString(MessageID.schedule))
                    }

                    schedules?.let { schedules ->
                        child(SchedulesComponent::class) {
                            attrs.entries = schedules
                        }
                    }
                }

            }
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }


    class SchedulesComponent(mProps: ListProps<Schedule>): UstadSimpleList<ListProps<Schedule>>(mProps){

        override fun RBuilder.renderListItem(item: Schedule) {
            /*styledDiv {
                val frequencyMessageId = ScheduleEditPresenter.FrequencyOption.values()
                    .firstOrNull { it.optionVal == item.scheduleFrequency }?.messageId ?: MessageID.None
                val dayMessageId = ScheduleEditPresenter.DayOptions.values()
                    .firstOrNull { it.optionVal == item.scheduleDay }?.messageId ?: MessageID.None

                val scheduleDays = "${systemImpl.getString(frequencyMessageId, this)} - ${systemImpl.getString(dayMessageId, this)}"

                val startEndTime = "${Date(item.sceduleStartTime).formattedInHoursAndMinutes()} " +
                        "- ${Date(item.scheduleEndTime).formattedInHoursAndMinutes()}"

                mTypography("$scheduleDays $startEndTime",
                    variant = MTypographyVariant.body2,
                    color = MTypographyColor.textPrimary,
                    gutterBottom = true){
                    css(alignTextToStart)
                }
            }*/
        }

    }
}