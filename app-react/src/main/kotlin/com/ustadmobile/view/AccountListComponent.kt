package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.button.mIconButton
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.controller.AccountListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.AccountListView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.createCreateNewItem
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umProfileAvatar
import kotlinx.css.Cursor
import kotlinx.css.cursor
import kotlinx.css.marginTop
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledDiv

class AccountListComponent(mProps: RProps): UstadBaseComponent<RProps, RState>(mProps), AccountListView  {

    private var mPresenter: AccountListPresenter? = null

    override val viewName: String
        get() = AccountListView.VIEW_NAME

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

    override var accountListLive: DoorLiveData<List<UserSessionWithPersonAndEndpoint>>? = null
        set(value) {
            field?.removeObserver(accountListObserver)
            field = value
            value?.observe(this, accountListObserver)
        }

    override var activeAccountLive: DoorLiveData<UserSessionWithPersonAndEndpoint?>? = null
        set(value) {
            field?.removeObserver(activeAccountObserver)
            field = value
            value?.observe(this, activeAccountObserver)
        }

    override var intentMessage: String? = null
        set(value) {
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
                renderList(listOf(accounts), true)
            }

            mCurrentStoredAccounts?.let { accounts ->
                renderList(accounts)
            }

            styledDiv {
                css {
                    marginTop = 3.spacingUnits
                    cursor = Cursor.pointer
                }

                attrs.onClickFunction = {
                    mPresenter?.handleClickAddAccount()
                }

                createCreateNewItem(getString(MessageID.add_another)
                    .format(getString(MessageID.account)))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        mActiveAccount = null
    }

    private fun RBuilder.renderList(accounts: List<UserSessionWithPersonAndEndpoint>, active: Boolean = false){
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

    interface AccountPros: ListProps<UserSessionWithPersonAndEndpoint>{
        var activeAccount: Boolean
    }

    class AccountListComponent(mProps: AccountPros): UstadSimpleList<AccountPros>(mProps){

        override fun RBuilder.renderListItem(item: UserSessionWithPersonAndEndpoint) {
            val presenter = props.presenter as AccountListPresenter

            umGridContainer {
               umItem(MGridSize.cells3, MGridSize.cells2){
                   umProfileAvatar(item.person.personUid, "person")
               }

               umItem(if(!props.activeAccount) MGridSize.cells7 else MGridSize.cells9,
                   if(!props.activeAccount) MGridSize.cells8 else MGridSize.cells10){

                   umItem(MGridSize.cells12){
                       mTypography(item.person.fullName(),
                           variant = MTypographyVariant.body1){
                           css(StyleManager.alignTextToStart)
                       }
                   }

                   umItem(MGridSize.cells12){

                       umGridContainer {
                           umItem(MGridSize.cells2,MGridSize.cells1){
                               mIcon("person")
                           }

                           umItem(MGridSize.cells10, MGridSize.cells3){
                               mTypography(item.person.username,
                                   variant = MTypographyVariant.body2){
                                   css(StyleManager.alignTextToStart)
                               }
                           }

                           umItem(MGridSize.cells2,MGridSize.cells1){
                               mIcon("link")
                           }

                           umItem(MGridSize.cells10, MGridSize.cells7){
                               mTypography(item.endpoint.url,
                                   variant = MTypographyVariant.body2){
                                   css(StyleManager.alignTextToStart)
                               }
                           }
                       }
                   }

                   umItem(MGridSize.cells12){
                       css(defaultMarginTop)
                       umGridContainer(MGridSpacing.spacing2) {
                           umItem(MGridSize.cells7,MGridSize.cells4){
                               mButton(getString(MessageID.my).format(getString(MessageID.profile)),
                                   size = MButtonSize.large,
                                   variant = MButtonVariant.outlined,
                                   color = MColor.primary,
                                   onClick = {
                                       it.stopPropagation()
                                       presenter.handleClickProfile(item.person.personUid)
                                   })
                           }

                           if(props.activeAccount){
                               umItem(MGridSize.cells5,MGridSize.cells4){
                                   mButton(getString(MessageID.logout),
                                       size = MButtonSize.large,
                                       variant = MButtonVariant.outlined,
                                       color = MColor.primary,
                                       onClick = {
                                           it.stopPropagation()
                                           presenter.handleClickLogout(item)
                                       })
                               }
                           }
                       }
                   }

               }

               if(!props.activeAccount){
                   umItem(MGridSize.cells2){
                       mIconButton("delete", onClick = {
                           presenter.handleClickDeleteSession(item)
                       })
                   }
               }
           }
        }

    }
}