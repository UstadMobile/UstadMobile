package com.ustadmobile.view

import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.controller.AccountListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.AccountListView
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.defaultPaddingTopBottom
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.Util
import com.ustadmobile.util.Util.stopEventPropagation
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.renderCreateNewItemOnList
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umProfileAvatar
import kotlinx.css.*
import mui.material.ButtonColor
import mui.material.ButtonVariant
import mui.material.Size
import mui.material.styles.TypographyVariant
import org.w3c.dom.events.Event
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import styled.styledSpan

class AccountListComponent(mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps), AccountListView  {

    private var mPresenter: AccountListPresenter? = null

    private var mCurrentStoredAccounts: List<UserSessionWithPersonAndEndpoint>? = null

    private var mActiveAccount: UserSessionWithPersonAndEndpoint? = null

    private var accountListObserver = ObserverFnWrapper<List<UserSessionWithPersonAndEndpoint>?> {
        setState {
            mCurrentStoredAccounts = it
        }
    }

    private var activeAccountObserver = ObserverFnWrapper<UserSessionWithPersonAndEndpoint?> {
        if(it != null){
            setState {
                mActiveAccount = it
            }
        }
    }

    override var accountListLive: LiveData<List<UserSessionWithPersonAndEndpoint>>? = null
        set(value) {
            field?.removeObserver(accountListObserver)
            field = value
            value?.observe(this, accountListObserver)
        }

    override var activeAccountLive: LiveData<UserSessionWithPersonAndEndpoint?>? = null
        set(value) {
            field?.removeObserver(activeAccountObserver)
            field = value
            value?.observe(this, activeAccountObserver)
        }

    override var intentMessage: String? = null
        set(value) {
            field = value
        }

    override var title: String? = null
        set(value) {
            ustadComponentTitle = value
            field = value
        }

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = AccountListPresenter(this,arguments,this, di,
            this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            css{
                +defaultPaddingTop
                +contentContainer
            }

            mActiveAccount?.let { accounts ->
                renderAccountList(listOf(accounts), true)
            }

            mCurrentStoredAccounts?.let { accounts ->
                renderAccountList(accounts)
            }

            styledDiv {
                css {
                    marginTop = 3.spacingUnits
                    cursor = Cursor.pointer
                }

                umListItem(button = true) {
                    attrs.onClick = {
                        Util.stopEventPropagation(it)
                        mPresenter?.handleClickAddAccount()
                    }
                    renderCreateNewItemOnList(getString(MessageID.add_another)
                        .format(getString(MessageID.account)))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        mActiveAccount = null
    }

    private fun RBuilder.renderAccountList(accounts: List<UserSessionWithPersonAndEndpoint>, active: Boolean = false){
        child(AccountListComponent::class){
            attrs.entries = accounts
            attrs.activeAccount = active
            mPresenter?.let {
                attrs.presenter = it
            }
            attrs.onEntryClicked = {
                mPresenter
            }
        }
    }

    interface AccountPros: SimpleListProps<UserSessionWithPersonAndEndpoint>{
        var activeAccount: Boolean
    }

    class AccountListComponent(mProps: AccountPros): UstadSimpleList<AccountPros>(mProps){

        override fun RBuilder.renderListItem(item: UserSessionWithPersonAndEndpoint, onClick: (Event) -> Unit) {
            val presenter = props.presenter as AccountListPresenter

            umGridContainer {
                css(defaultPaddingTopBottom)
                attrs.onClick = {
                    stopEventPropagation(it)
                    presenter.handleClickUserSession(item)
                }
                umItem(GridSize.cells3, GridSize.cells2){
                    umProfileAvatar(item.person.personUid, "person")
                }

                umItem(if(!props.activeAccount) GridSize.cells7 else GridSize.cells9,
                    if(!props.activeAccount) GridSize.cells8 else GridSize.cells10){

                    umItem(GridSize.cells12){
                        umTypography(item.person.fullName(),
                            variant = TypographyVariant.body1){
                            css(StyleManager.alignTextToStart)
                        }
                    }

                    umItem(GridSize.cells12){

                        umGridContainer {
                            umItem(GridSize.cells2, GridSize.cells1){
                                umIcon("person")
                            }

                            umItem(GridSize.cells10, GridSize.cells3){
                                umTypography(item.person.username,
                                    variant = TypographyVariant.body2){
                                    css(StyleManager.alignTextToStart)
                                }
                            }

                            umItem(GridSize.cells2, GridSize.cells1){
                                umIcon("link")
                            }

                            umItem(GridSize.cells10, GridSize.cells7){
                                umTypography(item.endpoint.url,
                                    variant = TypographyVariant.body2){
                                    css(StyleManager.alignTextToStart)
                                }
                            }
                        }
                    }

                    umItem(GridSize.cells12){
                        css(defaultMarginTop)
                        umGridContainer(GridSpacing.spacing2) {
                            umItem(GridSize.cells7, GridSize.cells4){
                                umButton(getString(MessageID.my).format(getString(MessageID.profile)),
                                    size = Size.large,
                                    variant = ButtonVariant.outlined,
                                    color = ButtonColor.primary,
                                    onClick = {
                                        it.stopPropagation()
                                        presenter.handleClickProfile(item.person.personUid)
                                    })
                            }

                            if(props.activeAccount){
                                umItem(GridSize.cells5, GridSize.cells4){
                                    umButton(getString(MessageID.logout),
                                        size = Size.large,
                                        variant = ButtonVariant.outlined,
                                        color = ButtonColor.primary,
                                        onClick = {
                                            presenter.handleClickLogout(item)
                                        })
                                }
                            }
                        }
                    }

                }

                if(!props.activeAccount){
                    umItem(GridSize.cells2){
                        styledSpan {
                            css{
                                width = 50.px
                            }
                            umIconButton("delete",
                                id = "delete_account_btn",
                                onClick = {
                                    presenter.handleClickDeleteSession(item)
                                })
                        }
                    }
                }
            }
        }

    }
}