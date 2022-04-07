package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.CourseBlockWithCompleteEntity
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.mui.components.TypographyVariant
import com.ustadmobile.mui.components.umTypography
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.Util.ASSET_ENTRY
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.formatDateRange
import com.ustadmobile.view.ext.*
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ClazzDetailOverviewComponent(mProps: UmProps): UstadDetailComponent<ClazzWithDisplayDetails>(mProps),
    ClazzDetailOverviewView {

    private var mPresenter: ClazzDetailOverviewPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override val viewNames: List<String>
        get() = listOf(ClazzDetailOverviewView.VIEW_NAME)

    private var schedules: List<Schedule> = listOf()

    private val observer = ObserverFnWrapper<List<Schedule>>{
        if(it.isEmpty()) return@ObserverFnWrapper
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
    override var courseBlockList: DoorDataSourceFactory<Int, CourseBlockWithCompleteEntity>?
        get() = TODO("Not yet implemented")
        set(value) {}

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
        styledDiv {
            css {
                +defaultPaddingTop
                +contentContainer
            }

            umGridContainer(columnSpacing = GridSpacing.spacing6) {
                umItem(GridSize.cells12, GridSize.cells4){
                    umEntityAvatar(listItem = true, fallbackSrc = ASSET_ENTRY)
                }

                umItem(GridSize.cells12, GridSize.cells8){
                    umGridContainer {

                        umItem(GridSize.cells12){
                            umTypography(entity?.clazzDesc,
                                variant = TypographyVariant.body1,
                                gutterBottom = true){
                                css(StyleManager.alignTextToStart)
                            }

                        }

                        val numOfStudentTeachers = getString(MessageID.x_teachers_y_students)
                            .format(entity?.numTeachers ?: 0, entity?.numStudents ?: 0)

                        createInformation("people", numOfStudentTeachers, getString(MessageID.members))

                        createInformation("login", entity?.clazzCode ?: "", getString(MessageID.class_code)){
                            Util.copyToClipboard(entity?.clazzCode ?: "") {
                                showSnackBar(getString(MessageID.copied_to_clipboard))
                            }
                        }

                        createInformation("school", entity?.clazzSchool?.schoolName)

                        createInformation("event", entity?.clazzStartTime.formatDateRange(entity?.clazzEndTime))

                        createInformation("event", entity?.clazzHolidayCalendar?.umCalendarName)


                        if(!schedules.isNullOrEmpty()){
                            umItem(GridSize.cells12){
                                createListSectionTitle(getString(MessageID.schedule))
                            }

                            renderSchedules(schedules = schedules, withDelete = false)
                        }

                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }
}