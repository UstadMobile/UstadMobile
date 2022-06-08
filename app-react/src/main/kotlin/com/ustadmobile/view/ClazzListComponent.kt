package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.roleToString
import com.ustadmobile.core.util.ext.roundTo
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.clazzListRoleChip
import com.ustadmobile.util.StyleManager.gridListSecondaryItemDesc
import com.ustadmobile.util.StyleManager.gridListSecondaryItemIcons
import com.ustadmobile.util.StyleManager.maxLines
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.wordBreakLimit
import com.ustadmobile.view.ext.*
import kotlinx.css.*
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class ClazzListComponent (props: UmProps): UstadListComponent<Clazz,
        ClazzWithListDisplayDetails>(props), ClazzList2View {

    private var mPresenter: ClazzListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzDao

    override val listPresenter: UstadListPresenter<*, in ClazzWithListDisplayDetails>?
        get() = mPresenter

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.text = getString(MessageID.course)
        ustadComponentTitle = getString(MessageID.courses)
        linearLayout = false
        addNewEntryText = getString(MessageID.add_a_new_class)
        mPresenter = ClazzListPresenter(this, arguments, this,di,this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: ClazzWithListDisplayDetails) {
        styledDiv {
            css{
                position = Position.relative
            }

            withAttachmentLocalUrlLookup(item.clazzUid,
                ClazzDetailOverviewComponent.CLAZZ_PICTURE_LOOKUP_ADAPTER,
            ) { attachmentSrc ->
                umEntityAvatar(
                    src = attachmentSrc,
                    className = "${StyleManager.name}-clazzItemClass",
                    listItem = true,
                    fallbackSrc = "assets/entry_placeholder.jpeg")
            }

            val memberRole = "${item.clazzActiveEnrolment?.roleToString(this,systemImpl)}"

            if(item.clazzActiveEnrolment != null){
                umChip(memberRole,color = ChipColor.primary){
                    css(clazzListRoleChip)
                    attrs.asDynamic().icon = umIcon("badge"){
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

            umTypography(item.clazzName, TypographyVariant.h6){
                css{
                    +alignTextToStart
                    maxLines(this, 1)
                }
            }

            umTypography(item.clazzDesc?.wordBreakLimit(), TypographyVariant.body1){
                css{
                    +alignTextToStart
                   if(!item.clazzDesc.isNullOrEmpty()){
                       maxLines(this, 2)
                   }
                }
            }

            umGridContainer{
                umItem(GridSize.cells1){
                    umIcon("people", color = IconColor.inherit){
                        css(gridListSecondaryItemIcons)
                    }
                }

                umItem(GridSize.cells6){
                    val numOfStudentTeachers = getString(MessageID.x_teachers_y_students)
                        .format(item.numTeachers, item.numStudents)
                    umTypography(numOfStudentTeachers){
                        css{
                            +alignTextToStart
                            +gridListSecondaryItemDesc
                        }
                    }

                }

                umItem(GridSize.cells1){
                    statusCircleIndicator(item.attendanceAverage)
                }

                umItem(GridSize.cells4){
                    val attendancesPercentage = getString(MessageID.x_percent_attended)
                        .format((if(item.attendanceAverage >= 0) item.attendanceAverage * 100 else 0f).roundTo())
                    umTypography(attendancesPercentage){
                        css{
                            +alignTextToStart
                            +gridListSecondaryItemDesc
                        }
                    }

                }
            }
        }
    }

    override fun handleClickAddNewEntry() {
        var args = mutableMapOf<String, String>()
        val filterExcludeMembersOfSchool =
            arguments[PersonListView.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL]?.toLong() ?: 0L
        if(filterExcludeMembersOfSchool != 0L){
            args = mutableMapOf(UstadView.ARG_SCHOOL_UID to filterExcludeMembersOfSchool.toString())
        }
        args.putAll(arguments)
        mPresenter?.handleClickAddNewItem(args)
    }

    override fun handleClickEntry(entry: ClazzWithListDisplayDetails) {
        mPresenter?.onClickClazz(entry)
    }

    override fun onFabClicked() {
        setState {
            showAddEntryOptions = true
        }
    }

    override fun RBuilder.renderAddContentOptionsDialog() {
        if(showAddEntryOptions){
            val options = if(newClazzListOptionVisible){
                listOf(UmDialogOptionItem("add",MessageID.add_a_new_course) {
                    mPresenter?.handleClickCreateNewFab()
                })
            }else {
                listOf()
            } + listOf(UmDialogOptionItem("login",MessageID.join_existing_course){
                mPresenter?.handleClickJoinClazz()
            })

            renderDialogOptions(systemImpl,options, Date().getTime().toLong()){
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