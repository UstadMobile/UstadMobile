package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ustadmobile.core.controller.SchoolListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SchoolListView
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.SchoolWithMemberCountAndLocation
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.contentAfterIconMarginLeft
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.umItemWithIconAndText
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umProfileAvatar
import kotlinx.css.*
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import kotlin.js.Date

class SchoolListComponent(mProps: RProps) : UstadListComponent<School, SchoolWithMemberCountAndLocation>(mProps),
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

    override fun onCreate() {
        super.onCreate()
        fabManager?.text = getString(MessageID.school)
        mPresenter = SchoolListPresenter(this, arguments,
            this, di, this)
        createNewTextId = MessageID.add_a_new_school
        showCreateNewItem = true
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: SchoolWithMemberCountAndLocation) {
        umGridContainer(MGridSpacing.spacing5) {
            css{
                paddingTop = 4.px
                paddingBottom = 4.px
            }
            umItem(MGridSize.cells3, MGridSize.cells1){
                umProfileAvatar(item.schoolUid, "school")
            }

            umItem(MGridSize.cells9, MGridSize.cells11){
                umItem(MGridSize.cells12){
                    mTypography(item.schoolName,
                        variant = MTypographyVariant.h6,
                        color = MTypographyColor.textPrimary){
                        css (StyleManager.alignTextToStart)
                    }
                }

               mGridContainer {
                   css(defaultMarginTop)
                   umItem(MGridSize.cells12, MGridSize.cells6){
                       css(umItemWithIconAndText)

                       mIcon("place",fontSize = MIconFontSize.small)

                       mTypography(item.schoolAddress, variant = MTypographyVariant.body1,
                           paragraph = true,
                           color = MTypographyColor.textPrimary){
                           css{
                               +alignTextToStart
                               +contentAfterIconMarginLeft
                           }
                       }
                   }

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
}