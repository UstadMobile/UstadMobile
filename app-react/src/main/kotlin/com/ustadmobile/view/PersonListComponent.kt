package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ustadmobile.core.controller.PersonListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umProfileAvatar
import kotlinx.css.paddingBottom
import kotlinx.css.paddingTop
import kotlinx.css.px
import react.RBuilder
import react.RProps
import styled.css


class PersonListComponent(mProps: RProps): UstadListComponent<Person, PersonWithDisplayDetails>(mProps),
    PersonListView{

    private lateinit var mPresenter: PersonListPresenter

    override val displayTypeRepo: Any?
        get() = dbRepo?.personDao

    override val viewName: String
        get() = PersonListView.VIEW_NAME


    override val listPresenter: UstadListPresenter<*, in PersonWithDisplayDetails>
        get() = mPresenter


    override fun onComponentReady() {
        super.onComponentReady()
        fabState.title = getString(MessageID.person)
        if(arguments.containsKey(UstadView.ARG_CODE_TABLE)){
            //handle invite with link
        }

        mPresenter = PersonListPresenter(this, arguments, this,di,this)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: PersonWithDisplayDetails) {

        umGridContainer(MGridSpacing.spacing5) {
            css{
                paddingTop = 4.px
                paddingBottom = 4.px
            }
            umItem(MGridSize.cells3, MGridSize.cells1){
                umProfileAvatar(item.personUid, "person")
            }

            umItem(MGridSize.cells9, MGridSize.cells11){
                umItem(MGridSize.cells12){
                    mTypography("${item.firstNames} ${item.lastName}",
                        variant = MTypographyVariant.h6,
                        color = MTypographyColor.textPrimary){
                        css (alignTextToStart)
                    }
                }

                umItem(MGridSize.cells12){
                    mTypography("", variant = MTypographyVariant.body1,
                        paragraph = true,
                        color = MTypographyColor.textPrimary){
                        css(alignTextToStart)
                    }
                }
            }
        }
    }


    override fun handleClickEntry(entry: PersonWithDisplayDetails) {
        mPresenter.handleClickEntry(entry)
    }

}