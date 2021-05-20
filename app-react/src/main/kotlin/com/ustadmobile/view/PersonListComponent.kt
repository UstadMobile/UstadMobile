package com.ustadmobile.view

import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.mTypography
import com.ustadmobile.core.controller.PersonListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.util.CssStyleManager.alignTextToStart
import com.ustadmobile.util.CssStyleManager.personListItemContainer
import com.ustadmobile.util.CssStyleManager.personListItemInfo
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.view.ext.renderAvatar
import react.RBuilder
import react.RProps
import styled.css
import styled.styledDiv

interface PersonListProps: RProps {
    var args: Map<String,String>
}

class PersonListComponent(mProps: PersonListProps): UstadListComponent<Person, PersonWithDisplayDetails>(mProps),
    PersonListView{

    private lateinit var mPresenter: PersonListPresenter

    override val displayTypeRepo: Any?
        get() = dbRepo?.personDao

    override val viewName: String
        get() = PersonListView.VIEW_NAME


    override val listPresenter: UstadListPresenter<*, in PersonWithDisplayDetails>?
        get() = mPresenter


    override fun onComponentReady() {
        super.onComponentReady()
        fabState.label = systemImpl.getString(MessageID.person, this)
        if(getArgs().containsKey(UstadView.ARG_CODE_TABLE)){

        }

        mPresenter = PersonListPresenter(this, getArgs(), this,di,this)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: PersonWithDisplayDetails) {
        styledDiv{
            css(personListItemContainer)
            renderAvatar(item.personUid, "person")

            styledDiv {
                css(personListItemInfo)
                mTypography("${item.firstNames} ${item.lastName}",variant = MTypographyVariant.h6){
                    css { +alignTextToStart }
                }

                mTypography("", variant = MTypographyVariant.body1, paragraph = true){
                    css(alignTextToStart)
                }
            }
        }
    }


    override fun handleClickEntry(entry: PersonWithDisplayDetails) {
        mPresenter.handleClickEntry(entry)
    }

}