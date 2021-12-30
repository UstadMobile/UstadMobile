package com.ustadmobile.view

import com.ustadmobile.core.controller.SchoolListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SchoolListView
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.SchoolWithMemberCountAndLocation
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.gridListSecondaryItemDesc
import com.ustadmobile.util.StyleManager.gridListSecondaryItemIcons
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.wordBreakLimit
import com.ustadmobile.view.ext.umEntityAvatar
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.Position
import kotlinx.css.display
import kotlinx.css.padding
import kotlinx.css.position
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class SchoolListComponent(mProps: UmProps) : UstadListComponent<School, SchoolWithMemberCountAndLocation>(mProps),
    SchoolListView{

    private var mPresenter: SchoolListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.schoolDao

    override val listPresenter: UstadListPresenter<*, in SchoolWithMemberCountAndLocation>?
        get() = mPresenter

    override val viewNames: List<String>
        get() = listOf(SchoolListView.VIEW_NAME)

    override var newSchoolListOptionVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        linearLayout = false
        fabManager?.text = getString(MessageID.school)
        mPresenter = SchoolListPresenter(this, arguments,
            this, di, this)
        createNewText = getString(MessageID.add_a_new_school)
        showCreateNewItem = true
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: SchoolWithMemberCountAndLocation) {

        styledDiv {
            css{
                position = Position.relative
            }

            umEntityAvatar(
                className = "${StyleManager.name}-clazzItemClass",
                listItem = true,
                fallbackSrc = "assets/entry_placeholder.jpeg")

        }
        styledDiv {
            css {
                padding(2.spacingUnits)
            }

            umTypography(item.schoolName,
                variant = TypographyVariant.h6){
                css(alignTextToStart)
            }

            umTypography(item.schoolDesc?.wordBreakLimit(),
                variant = TypographyVariant.body1){
                css{
                    display = displayProperty(item.schoolDesc != null, true)
                    +alignTextToStart
                }
            }

            umGridContainer{
                if(item.schoolAddress?.isNotEmpty() == true){
                    umItem(GridSize.cells1){
                        umIcon("place", color = IconColor.inherit){
                            css(gridListSecondaryItemIcons)
                        }
                    }

                    umItem(GridSize.cells11){
                        umTypography(item.schoolAddress){
                            css{
                                +alignTextToStart
                                +gridListSecondaryItemDesc
                            }
                        }

                    }
                }

                umItem(GridSize.cells1){
                    umIcon("people", color = IconColor.inherit){
                        css(gridListSecondaryItemIcons)
                    }
                }

                umItem(GridSize.cells11){
                    val numOfStudentTeachers = getString(MessageID.x_teachers_y_students)
                        .format(item.numTeachers, item.numStudents)
                    umTypography(numOfStudentTeachers){
                        css{
                            +alignTextToStart
                            +gridListSecondaryItemDesc
                        }
                    }

                }
            }
        }
    }

    override fun onFabClicked() {
        setState {
            showAddEntryOptions = true
        }
    }

    override fun RBuilder.renderAddContentOptionsDialog() {
        if(showAddEntryOptions){
            val options = mutableListOf(
                UmDialogOptionItem("add", MessageID.add_a_new_school) {
                    mPresenter?.handleClickCreateNewFab()
                },
                UmDialogOptionItem("login",MessageID.join_existing_school) {
                    mPresenter?.handleClickJoinSchool()
                }
            )

            renderDialogOptions(systemImpl,options, Date().getTime().toLong()){
                setState {
                    showAddEntryOptions = false
                }
            }
        }
    }

    override fun handleClickEntry(entry: SchoolWithMemberCountAndLocation) {
        mPresenter?.handleClickEntry(entry)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}