package com.ustadmobile.view

import androidx.paging.DataSource
import com.ccfraser.muirwik.components.*
import com.ustadmobile.core.controller.ClazzDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultDoubleMarginTop
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.formatDate
import com.ustadmobile.view.ext.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.Cursor
import kotlinx.css.cursor
import kotlinx.css.display
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class ClazzDetailOverviewComponent(mProps: RProps): UstadDetailComponent<ClazzWithDisplayDetails>(mProps),
    ClazzDetailOverviewView {

    private var mPresenter: ClazzDetailOverviewPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override val viewName: String
        get() = ClazzDetailOverviewView.VIEW_NAME

    private var schedules: List<Schedule>? = null

    override var scheduleList: DataSource.Factory<Int, Schedule>? = null
        set(value) {
            field = value
            GlobalScope.launch {
                val data = value?.getData(0,1000)
                setState {
                    schedules = data
                }
            }
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

    override fun onCreate() {
        super.onCreate()
        editButtonMode = EditButtonMode.FAB
        mPresenter = ClazzDetailOverviewPresenter(this, arguments, this,
            di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
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

                val dateFormat = "DD/MM/YYYYY"
                val dateTxt = Date(entity?.clazzStartTime ?: 0).formatDate(dateFormat) +
                        " - ${Date(entity?.clazzEndTime ?: 0).formatDate(dateFormat)}"

                createInformation("event", dateTxt)

                createInformation("event", entity?.clazzHolidayCalendar?.umCalendarName)


                umItem(MGridSize.cells12){
                    css{
                        +defaultDoubleMarginTop
                        display = displayProperty(!schedules.isNullOrEmpty())
                    }
                    mTypography(getString(MessageID.schedule),
                        variant = MTypographyVariant.caption,
                        color = MTypographyColor.textPrimary,
                        gutterBottom = true){
                        css(alignTextToStart)
                    }

                }

                schedules?.let {
                    renderSchedules(it)
                }

            }
        }
    }
}


class SchedulesComponent(mProps: ListProps<Schedule>): UstadList<Schedule>(mProps){

    override fun onCreate() {
        super.onCreate()
        list = props.entries
    }
    override fun RBuilder.renderListItem(item: Schedule) {
        styledDiv {
            setScheduleText(item,systemImpl)
        }
    }
}

fun RBuilder.renderSchedules(schedules: List<Schedule>) = child(SchedulesComponent::class) {
    attrs.entries = schedules
}