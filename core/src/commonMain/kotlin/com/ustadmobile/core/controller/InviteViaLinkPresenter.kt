package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toDeepLink
import com.ustadmobile.core.view.InviteViaLinkView
import com.ustadmobile.core.view.JoinWithCodeView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CODE
import com.ustadmobile.core.view.UstadView.Companion.ARG_CODE_TABLE
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_NAME
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class InviteViaLinkPresenter(context: Any, args: Map<String, String>, view: InviteViaLinkView, di: DI)
    : UstadBaseController<InviteViaLinkView>(context, args, view, di) {

    val accountManager: UstadAccountManager by instance()

    val dbRepo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)

    val systemImpl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val code = arguments[ARG_CODE].toString()

        val linkArgs = mapOf(ARG_CODE to code,
            ARG_CODE_TABLE to (arguments[ARG_CODE_TABLE] ?: ""))
        view.inviteLink = linkArgs.toDeepLink(accountManager.activeAccount.endpointUrl,
            JoinWithCodeView.VIEW_NAME)
        view.inviteCode = code
        view.entityName = arguments[ARG_ENTITY_NAME].toString()
    }





}