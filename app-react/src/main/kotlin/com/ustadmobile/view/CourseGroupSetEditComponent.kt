package com.ustadmobile.view

import com.ustadmobile.core.controller.CourseGroupSetEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.view.CourseGroupSetEditView
import com.ustadmobile.lib.db.entities.CourseGroupMemberPerson
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.renderListItemWithLeftIconTitleAndOptionOnRight
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umSpacer
import kotlinx.browser.window
import kotlinx.css.height
import kotlinx.css.px
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class CourseGroupSetEditComponent (mProps: UmProps): UstadEditComponent<CourseGroupSet>(mProps),
    CourseGroupSetEditView {

    private var mPresenter: CourseGroupSetEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseGroupSet>?
        get() = mPresenter

    private var groupLabel = FieldLabel(getString(MessageID.group))

    private var numberOfGroups = 0;


    override var memberList: List<CourseGroupMemberPerson>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var groupList: List<IdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    var groupNumberChangeTaskId = -1

    override var entity: CourseGroupSet? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    private var titleLabel = FieldLabel(text = getString(MessageID.title))

    private var numberOfLabel = FieldLabel(text = getString(MessageID.number_of_groups))


    override fun onCreateView() {
        super.onCreateView()
        mPresenter = CourseGroupSetEditPresenter(this, arguments, this,
            this,di)
        setEditTitle(MessageID.add_new_groups, MessageID.edit_group)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +contentContainer
                +defaultPaddingTop
            }

            umGridContainer(GridSpacing.spacing4) {
                umItem(GridSize.cells12, GridSize.cells6) {
                    umTextField(label = "${titleLabel.text}",
                        helperText = titleLabel.errorText,
                        value = entity?.cgsName,
                        error = titleLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.cgsName = it
                            }
                        }
                    )
                }

                umItem(GridSize.cells12, GridSize.cells3) {
                    umTextField(label = "${numberOfLabel.text}",
                        helperText = numberOfLabel.errorText,
                        value = entity?.cgsTotalGroups.toString(),
                        error = numberOfLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = { nOfGroups ->
                            val numberOfGroups = (nOfGroups.ifEmpty { "0" }).toInt()
                            setState{
                                entity?.cgsTotalGroups = numberOfGroups
                            }
                            window.clearTimeout(groupNumberChangeTaskId)
                            groupNumberChangeTaskId = window.setTimeout({
                                mPresenter?.handleNumberOfGroupsChanged(numberOfGroups)
                            }, 1000)
                        }
                    )
                }

                umItem(GridSize.cells12, GridSize.cells3) {
                    umButton(getString(MessageID.assign_to_random_groups),
                        size = ButtonSize.large,
                        color = UMColor.secondary,
                        variant = ButtonVariant.contained,
                        onClick = {
                            mPresenter?.handleAssignRandomGroupsClicked()
                        }){
                        css {
                            +defaultFullWidth
                            +defaultMarginTop
                            height = 50.px
                        }}
                }
            }

            umSpacer()

            umItem {
               umList {
                   css(horizontalList)
                   memberList?.forEachIndexed { index, groupMember ->
                       umListItem {
                           renderListItemWithLeftIconTitleAndOptionOnRight(
                               groupMember.member?.cgmGroupNumber.toString(),
                               "person",
                               "${groupMember.firstNames} ${groupMember.lastName}",
                               options = groupList,
                               fieldLabel = groupLabel,
                               onChange = {
                                   memberList!![index].member?.cgmGroupNumber = it.toInt()
                                   setState {  }
                               }
                           )
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