package com.ustadmobile.view

import com.ustadmobile.core.controller.SchoolListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SchoolListView
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.SchoolWithMemberCountAndLocation
import com.ustadmobile.util.UmProps
import react.RBuilder
import react.setState
import kotlin.js.Date

class SchoolListComponent(mProps: UmProps) : UstadListComponent<School, SchoolWithMemberCountAndLocation>(mProps),
    SchoolListView{

    private var mPresenter: SchoolListPresenter? = null


    override val displayTypeRepo: Any?
        get() = dbRepo?.schoolDao

    override val listPresenter: UstadListPresenter<*, in SchoolWithMemberCountAndLocation>?
        get() = mPresenter

    override val viewName: String
        get() = SchoolListView.VIEW_NAME

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

       /* styledDiv {
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

            mTypography(item.schoolName,
                variant = MTypographyVariant.h6,
                color = MTypographyColor.textPrimary){
                css(alignTextToStart)
            }

            mTypography(item.schoolDesc?.breakToWork(),
                variant = MTypographyVariant.body1,
                color = MTypographyColor.textPrimary
            ){
                css{
                    display = displayProperty(item.schoolDesc != null, true)
                    +alignTextToStart
                }
            }

            umGridContainer{
                umItem(MGridSize.cells1){
                    mIcon("place", color = MIconColor.inherit){
                        css(gridListSecondaryItemIcons)
                    }
                }

                umItem(MGridSize.cells11){
                    mTypography(item.schoolAddress,
                        color = MTypographyColor.textPrimary
                    ){
                        css{
                            +alignTextToStart
                            +gridListSecondaryItemDesc
                        }
                    }

                }

                umItem(MGridSize.cells1){
                    mIcon("people", color = MIconColor.inherit){
                        css(gridListSecondaryItemIcons)
                    }
                }

                umItem(MGridSize.cells11){
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
            }
        }*/
    }

    override fun onFabClicked() {
        setState {
            showAddEntryOptions = true
        }
    }

    override fun RBuilder.renderAddEntryOptionsDialog() {
        if(showAddEntryOptions){
            val options = mutableListOf(
                PopUpOptionItem("add", MessageID.add_a_new_school) {
                    mPresenter?.handleClickCreateNewFab()
                },
                PopUpOptionItem("login",MessageID.join_existing_school) {
                    mPresenter?.handleClickJoinSchool()
                }
            )

            renderChoices(systemImpl,options, Date().getTime().toLong()){
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