package com.ustadmobile.view

import com.ustadmobile.core.controller.PersonListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.util.UmProps
import com.ustadmobile.view.ext.renderPersonListItemWithNameAndUserName
import react.RBuilder


class PersonListComponent(mProps: UmProps): UstadListComponent<Person, PersonWithDisplayDetails>(mProps),
    PersonListView{

    private var mPresenter: PersonListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.personDao


    override val listPresenter: UstadListPresenter<*, in PersonWithDisplayDetails>?
        get() = mPresenter

    override var inviteViaLinkVisibile: Boolean
        get() = inviteNewText.isNotEmpty()
        set(value) {
            inviteNewText = if(value) {
                getString(MessageID.invite_with_link)
            }else {
                ""
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.text = getString(MessageID.person)
        if(arguments.containsKey(UstadView.ARG_CODE_TABLE)){
            inviteNewText = getString(MessageID.invite_with_link)
        }
        addNewEntryText = getString(MessageID.add_a_new_person)
        ustadComponentTitle = getString(MessageID.people)
        mPresenter = PersonListPresenter(this, arguments, this,di,this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: PersonWithDisplayDetails) {
        renderPersonListItemWithNameAndUserName(item)
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