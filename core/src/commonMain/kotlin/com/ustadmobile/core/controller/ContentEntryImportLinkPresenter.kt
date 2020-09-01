package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryImportLinkView
import io.ktor.client.call.receive
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpStatement
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
//import com.ustadmobile.port.sharedse.contentformats.ImportedContentEntryMetaData

class ContentEntryImportLinkPresenter(context: Any, arguments: Map<String, String>, view: ContentEntryImportLinkView,
                                      di: DI) :
        UstadBaseController<ContentEntryImportLinkView>(context, arguments, view, di) {

    private val impl: UstadMobileSystemImpl by instance()

    val accountManager: UstadAccountManager by instance()

    val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_DB)

    private val currentHttpClient = defaultHttpClient()

    fun handleClickDone(link: String) {
        view.showHideProgress(true)
       // TODO check if link is valid first



        GlobalScope.launch {

            delay(1000)
            view.showHideProgress(false)

            currentHttpClient.post<HttpStatement>(){
                url(UMFileUtil.joinPaths(accountManager.activeAccount.endpointUrl),"/import/validateLink/")
                body = link
            }.execute(){

                val status = it.status
               // val data = it.receive<ImportedContentEntryMetaData>()



            }

        }




    }

}