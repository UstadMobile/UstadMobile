package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ustadmobile.core.controller.SchoolDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SchoolDetailOverviewView
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.contentAfterIconMarginLeft
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.umItemWithIconAndText
import com.ustadmobile.util.Util
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.*
import kotlinx.css.paddingBottom
import kotlinx.css.paddingTop
import kotlinx.css.px
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv

class SchoolDetailOverviewComponent(mProps: RProps): UstadDetailComponent<SchoolWithHolidayCalendar>(mProps),
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

            umGridContainer {

                umItem(MGridSize.cells12){
                    mTypography(entity?.schoolDesc,
                        variant = MTypographyVariant.body1,
                        color = MTypographyColor.textPrimary,
                        gutterBottom = true){
                        css(alignTextToStart)
                    }

                }

                createInformation("login", entity?.schoolCode ?: "", getString(MessageID.class_code)){
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
                    umItem(MGridSize.cells12){
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


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        schoolClazzList = null
        entity = null
    }

    class SchoolClazzesComponent(mProps: ListProps<ClazzWithListDisplayDetails>):
        UstadSimpleList<ListProps<ClazzWithListDisplayDetails>>(mProps){

        override fun RBuilder.renderListItem(item: ClazzWithListDisplayDetails) {
            umGridContainer(MGridSpacing.spacing5) {
                css{
                    paddingTop = 4.px
                    paddingBottom = 4.px
                }
                umItem(MGridSize.cells3, MGridSize.cells2){
                    umProfileAvatar(item.clazzUid, "group")
                }

                umItem(MGridSize.cells9, MGridSize.cells10){
                    umItem(MGridSize.cells12){
                        mTypography(item.clazzName,
                            variant = MTypographyVariant.body1,
                            color = MTypographyColor.textPrimary){
                            css (alignTextToStart)
                        }
                    }

                    mGridContainer {
                        css(defaultMarginTop)

                        umItem(MGridSize.cells12, MGridSize.cells6){
                            css(umItemWithIconAndText)

                            mIcon("people",fontSize = MIconFontSize.small)

                            val numOfStudentTeachers = getString(MessageID.x_teachers_y_students)
                                .format(item.numTeachers, item.numStudents)

                            mTypography(numOfStudentTeachers, variant = MTypographyVariant.body1,
                                paragraph = true,
                                color = MTypographyColor.textPrimary){
                                css{
                                    +alignTextToStart
                                    +contentAfterIconMarginLeft
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}



