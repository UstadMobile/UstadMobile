package com.ustadmobile.view

import com.ustadmobile.core.controller.PersonListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.mui.components.TypographyVariant
import com.ustadmobile.mui.components.umTypography
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.UmProps
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umProfileAvatar
import kotlinx.css.LinearDimension
import kotlinx.css.padding
import react.RBuilder
import styled.css


class PersonListComponent(mProps: UmProps): UstadListComponent<Person, PersonWithDisplayDetails>(mProps),
    PersonListView{

    private var mPresenter: PersonListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.personDao

    override val viewName: String
        get() = PersonListView.VIEW_NAME


    override val listPresenter: UstadListPresenter<*, in PersonWithDisplayDetails>?
        get() = mPresenter


    override fun onCreateView() {
        super.onCreateView()
        fabManager?.text = getString(MessageID.person)
        if(arguments.containsKey(UstadView.ARG_CODE_TABLE)){
            inviteNewText = getString(MessageID.invite_with_link)
        }
        createNewText = getString(MessageID.add_a_new_person)
        ustadComponentTitle = getString(MessageID.people)
        mPresenter = PersonListPresenter(this, arguments, this,di,this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: PersonWithDisplayDetails) {
        umGridContainer(GridSpacing.spacing5) {
            val padding = LinearDimension("4px")
            css{
                padding(top = padding, bottom = padding)
            }
            umItem(GridSize.cells3, GridSize.cells1){
                umProfileAvatar(item.personUid, "person")
            }

            umItem(GridSize.cells9, GridSize.cells11){
                umItem(GridSize.cells12){
                    umTypography("${item.firstNames} ${item.lastName}",
                        variant = TypographyVariant.h6){
                        css (alignTextToStart)
                    }
                }

                umItem(GridSize.cells12){
                    umTypography(if(item.username.isNullOrEmpty()) "" else "@${item.username}",
                        variant = TypographyVariant.body1,
                        paragraph = true){
                        css(alignTextToStart)
                    }
                }
            }
        }
    }

    override fun handleClickEntry(entry: PersonWithDisplayDetails) {
        mPresenter?.handleClickEntry(entry)
    }

    override fun handleInviteClicked() {
        mPresenter?.handleClickInviteWithLink()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }

}