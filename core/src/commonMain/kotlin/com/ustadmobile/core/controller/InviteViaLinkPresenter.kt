package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.InviteViaLinkView
import com.ustadmobile.core.view.JoinWithCodeView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CODE
import com.ustadmobile.core.view.UstadView.Companion.ARG_CODE_TABLE
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_NAME
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.School
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class InviteViaLinkPresenter(context: Any, args: Map<String, String>, view: InviteViaLinkView, di: DI)
    : UstadBaseController<InviteViaLinkView>(context, args, view, di) {

    val accountManager: UstadAccountManager by instance()

    val dbRepo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = TAG_REPO)

    val systemImpl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val code = arguments[ARG_CODE].toString()
        var apiUrl = accountManager.activeAccount.endpointUrl.trim()
        if(!apiUrl.endsWith("/")){
            apiUrl = "$apiUrl/"
        }
        val link = when (arguments[ARG_CODE_TABLE].toString().toInt()) {
            Clazz.TABLE_ID -> {
                "${apiUrl}umclient/${JoinWithCodeView.VIEW_NAME}?${UstadView.ARG_CODE}=$code&${UstadView.ARG_CODE_TABLE}=${Clazz.TABLE_ID.toString()}"
            }
            School.TABLE_ID -> {
                "${apiUrl}umclient/${JoinWithCodeView.VIEW_NAME}?${UstadView.ARG_CODE}=$code&${UstadView.ARG_CODE_TABLE}=${School.TABLE_ID.toString()}"
            }
            else -> {
                ""
            }
        }
        view.inviteLink = link
        view.entityName = arguments[ARG_ENTITY_NAME].toString()
    }





}