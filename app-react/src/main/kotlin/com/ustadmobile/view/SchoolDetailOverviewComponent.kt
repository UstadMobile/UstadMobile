package com.ustadmobile.view

import com.ustadmobile.core.controller.SchoolDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SchoolDetailOverviewView
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.*
import kotlinx.css.paddingBottom
import kotlinx.css.paddingTop
import kotlinx.css.px
import org.w3c.dom.events.Event
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class SchoolDetailOverviewComponent(mProps: UmProps): UstadDetailComponent<SchoolWithHolidayCalendar>(mProps),
    SchoolDetailOverviewView{

    private var mPresenter: SchoolDetailOverviewPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override val viewName: String
        get() = SchoolDetailOverviewView.VIEW_NAME

    private var schoolClazzList: List<ClazzWithListDisplayDetails>? = null

    private val observer = ObserverFnWrapper<List<ClazzWithListDisplayDetails>>{
        setState {
            schoolClazzList = it
        }
    }

    override var schoolClazzes: DoorDataSourceFactory<Int, ClazzWithListDisplayDetails>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(observer)
            liveData?.observe(this, observer)
        }

    override var schoolCodeVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: SchoolWithHolidayCalendar? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = SchoolDetailOverviewPresenter(this, arguments, this, di, this)
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
                    umEntityAvatar(listItem = true,
                        fallbackSrc = Util.ASSET_ENTRY,
                        iconName = "people",
                        showIcon = true)
                }

                umItem(GridSize.cells12, GridSize.cells8){
                    umGridContainer {

                        umItem(GridSize.cells12){
                            umTypography(entity?.schoolDesc,
                                variant = TypographyVariant.body1,
                                gutterBottom = true){
                                css(alignTextToStart)
                            }

                        }

                        createInformation("login", entity?.schoolCode ?: "",
                            getString(MessageID.school_code)){
                            Util.copyToClipboard(entity?.schoolCode ?: "") {
                                showSnackBar(getString(MessageID.copied_to_clipboard))
                            }
                        }

                        createInformation("place", entity?.schoolAddress,
                            getString(MessageID.address))

                        createInformation("call", entity?.schoolPhoneNumber,
                            getString(MessageID.phone_number))

                        createInformation("perm_contact_calendar", entity?.holidayCalendar?.umCalendarName,
                            getString(MessageID.holiday_calendar))

                        createInformation("email", entity?.schoolEmailAddress,
                            getString(MessageID.email)){
                            onClickEmail(entity?.schoolEmailAddress)
                        }

                        createInformation("language", entity?.schoolTimeZone,
                            getString(MessageID.timezone))

                        if(!schoolClazzList.isNullOrEmpty()){
                            umItem(GridSize.cells12){
                                createListSectionTitle(getString(MessageID.classes))
                            }

                            schoolClazzList?.let { classes ->
                                child(SchoolClazzesComponent::class) {
                                    attrs.entries = classes
                                    attrs.onEntryClicked = { clazz->
                                        mPresenter?.handleClickClazz(clazz)
                                    }
                                }
                            }
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
        schoolClazzList = null
        entity = null
    }

    class SchoolClazzesComponent(mProps: ListProps<ClazzWithListDisplayDetails>):
        UstadSimpleList<ListProps<ClazzWithListDisplayDetails>>(mProps){

        override fun RBuilder.renderListItem(item: dynamic, onClick: (Event) -> Unit) {
            umGridContainer(GridSpacing.spacing5) {
                attrs.onClick  = {
                    onClick.invoke(it.nativeEvent)
                }
                css{
                    paddingTop = 4.px
                    paddingBottom = 4.px
                }
                umItem(GridSize.cells3, GridSize.cells2){
                    umProfileAvatar(item.clazzUid, "group")
                }

                umItem(GridSize.cells9, GridSize.cells10){
                    umItem(GridSize.cells12){
                        umTypography(item.clazzName,
                            variant = TypographyVariant.body1){
                            css (alignTextToStart)
                        }
                    }

                    umGridContainer {
                        css(StyleManager.defaultMarginTop)

                        umItem(GridSize.cells12, GridSize.cells6){
                            css(StyleManager.umItemWithIconAndText)

                            umIcon("people",fontSize = IconFontSize.small)

                            val numOfStudentTeachers = getString(MessageID.x_teachers_y_students)
                                .format(item.numTeachers, item.numStudents)

                            umTypography(numOfStudentTeachers, variant = TypographyVariant.body1,
                                paragraph = true){
                                css{
                                    +alignTextToStart
                                    +StyleManager.contentAfterIconMarginLeft
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}



