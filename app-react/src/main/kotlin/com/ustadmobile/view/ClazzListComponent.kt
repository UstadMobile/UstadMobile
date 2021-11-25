package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import react.RBuilder
import com.ustadmobile.util.*
import react.setState
import kotlin.js.Date

class ClazzListComponent (props: UmProps): UstadListComponent<Clazz,
        ClazzWithListDisplayDetails>(props), ClazzList2View {

    private var mPresenter: ClazzListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzDao

    override val viewName: String
        get() = ClazzList2View.VIEW_NAME

    override val listPresenter: UstadListPresenter<*, in ClazzWithListDisplayDetails>?
        get() = mPresenter

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.text = getString(MessageID.clazz)
        title = getString(MessageID.classes)
        listTypeSingleColumn = false
        mPresenter = ClazzListPresenter(this, arguments, this,di,this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: ClazzWithListDisplayDetails) {
       /* styledDiv {
            css{
                position = Position.relative
            }
            umEntityAvatar(
                className = "${StyleManager.name}-clazzItemClass",
                listItem = true,
                fallbackSrc = "assets/entry_placeholder.jpeg")

            val memberRole = "${item.clazzActiveEnrolment?.roleToString(this,systemImpl)}"

            if(item.clazzActiveEnrolment != null){
                mChip(memberRole,color = MChipColor.primary){
                    css(clazzListRoleChip)
                    attrs.asDynamic().icon = mIcon("badge"){
                        css{
                            fontSize = LinearDimension("1.2em")
                        }
                    }
                }
            }
        }
        styledDiv {
            css {
                padding(2.spacingUnits)
            }

            mTypography(item.clazzName,
                variant = MTypographyVariant.h6,
                color = MTypographyColor.textPrimary){
                css(alignTextToStart)
            }

            mTypography(item.clazzDesc?.breakToWork(),
                variant = MTypographyVariant.body1,
                color = MTypographyColor.textPrimary
            ){
                css(alignTextToStart)
            }

            umGridContainer{
                umItem(MGridSize.cells1){
                    mIcon("people", color = MIconColor.inherit){
                        css(gridListSecondaryItemIcons)
                    }
                }

                umItem(MGridSize.cells6){
                    val numOfStudentTeachers = getString(MessageID.x_teachers_y_students)
                        .format(item.numTeachers, item.numStudents)
                    mTypography(numOfStudentTeachers,
                        color = MTypographyColor.textPrimary
                    ){
                        css{
                            +alignTextToStart
                            +gridListSecondaryItemDesc
                        }
                    }

                }

                umItem(MGridSize.cells1){
                    circleIndicator(item.attendanceAverage)
                }

                umItem(MGridSize.cells4){
                    val attendancesPercentage = getString(MessageID.x_percent_attended)
                        .format(item.attendanceAverage * 100)
                    mTypography(attendancesPercentage,
                        color = MTypographyColor.textPrimary
                    ){
                        css{
                            +alignTextToStart
                            +gridListSecondaryItemDesc
                        }
                    }

                }
            }
        }*/
    }

    override fun handleClickEntry(entry: ClazzWithListDisplayDetails) {
        mPresenter?.onClickClazz(entry)
    }
    override fun onFabClicked() {
        setState {
            showAddEntryOptions = true
        }
    }

    override fun RBuilder.renderAddEntryOptionsDialog() {
        if(showAddEntryOptions){
            val options = if(newClazzListOptionVisible)
                mutableListOf(PopUpOptionItem("add",MessageID.add_a_new_class) {
                    mPresenter?.handleClickCreateNewFab()
                })
            else mutableListOf()

            options.add(PopUpOptionItem("login",MessageID.join_existing_class){
                mPresenter?.handleClickJoinClazz()}
            )
            renderChoices(systemImpl,options, Date().getTime().toLong()){
                setState {
                    showAddEntryOptions = false
                }
            }
        }
    }

    override var newClazzListOptionVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}