package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.storeProgressStatement
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.core.view.PDFContentView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class PDFContentPresenter(context: Any, arguments: Map<String, String>, view: PDFContentView,
                          di: DI)
    : UstadBaseController<PDFContentView>(context, arguments, view, di) {


    private var entry: ContentEntry? = null
    private var contentEntryUid: Long = 0
    internal var containerUid: Long = 0
    var clazzUid: Long = 0L

    val accountManager: UstadAccountManager by instance()

    val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_DB)

    val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)

    val statementEndpoint by on(accountManager.activeAccount).instance<XapiStatementEndpoint>()

    var timePageOpened = 0L

    lateinit var contextRegistration: String

    internal var pdfPath: String? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        contextRegistration = randomUuid().toString()

        contentEntryUid = arguments.getValue(UstadView.ARG_CONTENT_ENTRY_UID).toLong()
        containerUid = arguments.getValue(UstadView.ARG_CONTAINER_UID).toLong()
        clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0L

        view.loading = true
        GlobalScope.launch(doorMainDispatcher()) {
            entry = db.contentEntryDao.findByUidAsync(contentEntryUid)
            view.entry = entry
            view.pdfContainerUid = containerUid
        }

    }

    //TODO: Check
    fun updateProgress(pageNumber: Int, pageTotal: Int, started: Boolean = false) {

        if(accountManager.activeAccount.personUid == 0L){
            return
        }

        var pdfOpenedDuration = 0L
        if(started){
            // player pressed play, record start time
            timePageOpened = systemTimeInMillis()
        }else if(timePageOpened == 0L){
            // pdf never opened, dont send statement
            return
        }else if(!started && timePageOpened > 0){
            // pdf ended
            pdfOpenedDuration = systemTimeInMillis() - timePageOpened
            timePageOpened = 0
        }else {
            // unhandled cases
            return
        }


        GlobalScope.launch{
            val progress = (pageNumber.toFloat() / pageTotal * 100).toInt()
            entry?.also {
                statementEndpoint.storeProgressStatement(
                        accountManager.activeAccount, it, progress,
                        pdfOpenedDuration,contextRegistration, clazzUid)
            }
        }
    }

}
