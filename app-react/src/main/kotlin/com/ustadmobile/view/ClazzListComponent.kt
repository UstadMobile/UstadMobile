package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ClazzListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.roleToString
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.umEntityAvatar
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.*
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class ClazzListComponent (props: RProps): UstadListComponent<Clazz,
        ClazzWithListDisplayDetails>(props), ClazzList2View {

    private var mPresenter: ClazzListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzDao

    private var showAddEntryOptions = false

    override val viewName: String
        get() = ClazzList2View.VIEW_NAME

    override val listPresenter: UstadListPresenter<*, in ClazzWithListDisplayDetails>?
        get() = mPresenter

    override fun onCreate(arguments: Map<String, String>) {
        super.onCreate(arguments)
        val accountManager: UstadAccountManager by instance()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)
        fabManager?.text = getString(MessageID.clazz)
        fabManager?.onClickListener = {
            setState {
                showAddEntryOptions = true
            }
        }
        listType = LIST_TYPE_MULTI_COLUMN
        mPresenter = ClazzListPresenter(this, arguments, this,di,this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: ClazzWithListDisplayDetails) {
        styledDiv {
            css{
                position = Position.relative
            }
            umEntityAvatar(className = "${StyleManager.name}-clazzItemClass",
                listItem = true, fallbackSrc = "assets/entry_placeholder.jpeg")
            val memberRole = "${item.clazzActiveEnrolment?.roleToString(this,systemImpl)}"
            if(item.clazzActiveEnrolment != null){
                mChip(memberRole,color = MChipColor.primary){
                    css{
                        position = Position.absolute
                        right = 10.px
                        top = 10.px
                    }
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

            mTypography(item.clazzDesc,
                variant = MTypographyVariant.body1,
                color = MTypographyColor.textPrimary
            ){
                css(alignTextToStart)
            }

            umGridContainer{
                umItem(MGridSize.cells1){
                    mIcon("people", color = MIconColor.inherit){
                        css{
                            marginTop = 4.px
                            fontSize = LinearDimension("1.1em")
                        }
                    }
                }

                umItem(MGridSize.cells11){
                    val numOfStudentTeachers = getString(MessageID.x_teachers_y_students)
                        .format(item.numTeachers, item.numStudents)
                    mTypography(numOfStudentTeachers,
                        variant = MTypographyVariant.caption,
                        color = MTypographyColor.textPrimary
                    ){
                        css(alignTextToStart)
                    }

                }
            }
        }
    }

    override fun handleClickEntry(entry: ClazzWithListDisplayDetails) {
        mPresenter?.onClickClazz(entry)
    }

    override fun RBuilder.renderAddEntryOptions() {
        if(showAddEntryOptions){
            val options = if(newClazzListOptionVisible)
                mutableListOf(PopUpOptionItem("add",MessageID.add_a_new_class) { mPresenter?.handleClickCreateNewFab() })
            else mutableListOf()

            options.add(PopUpOptionItem("login",MessageID.join_existing_class){ mPresenter?.handleClickJoinClazz()})
            renderPopUpOptions(systemImpl,options, Date().getTime().toLong())
        }
    }


    override var newClazzListOptionVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
}