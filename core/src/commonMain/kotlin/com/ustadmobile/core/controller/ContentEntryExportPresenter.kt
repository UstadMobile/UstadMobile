package com.ustadmobile.core.controller

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.ContainerManagerCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryExportView
import com.ustadmobile.core.view.ContentEntryExportView.Companion.ARG_CONTENT_ENTRY_TITLE
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Container
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class ContentEntryExportPresenter(context: Any, arguments: Map<String, String>, view: ContentEntryExportView,
                                  di: DI)
    : UstadBaseController<ContentEntryExportView>(context, arguments, view, di), ContainerManagerCommon.ExportProgressListener {

    private var exporting: Boolean = false

    private var container: Container? = null

    private var destinationDir: String = ""

    var destinationZipFile: String = ""

    lateinit var manager: ContainerManager

    private var entryTile: String = arguments[ARG_CONTENT_ENTRY_TITLE] ?: ""

    private var entryUid: Long = (arguments[UstadView.ARG_CONTENT_ENTRY_UID] ?: "0").toLong()

    private val umRepo: UmAppDatabase by instance(tag = UmAppDatabase.TAG_REPO)

    private val umDb: UmAppDatabase by instance(tag = UmAppDatabase.TAG_DB)

    private val impl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        GlobalScope.launch {
            container =  umRepo.containerDao.getMostRecentContainerForContentEntry(entryUid)

            impl.getStorageDirs(context, object : UmResultCallback<List<UMStorageDir>> {
                override fun onDone(result: List<UMStorageDir>?) {
                    view.setUpStorageOptions(result!!)
                    destinationDir = result[0].dirURI!!
                    view.setDialogMessage(entryTile)
                    //view.checkFilePermissions()
                }
            })
        }
    }

    override fun onProcessing(progress: Int) {
        view.runOnUiThread(Runnable {
            view.updateExportProgress(progress)
        })
    }

    override fun onDone() {
        view.runOnUiThread(Runnable {
            view.dismissDialog()
        })
    }

    fun handleClickPositive(){
        exporting = true
        view.runOnUiThread(Runnable { view.prepareProgressView(true)})
        if(container != null){
            manager = ContainerManager(container!!,umDb,umRepo,destinationDir, mutableMapOf())
            destinationZipFile = "$destinationDir/${entryTile.replace(" ","_")}_$entryUid.zip"
            manager.exportContainer(destinationZipFile, this)
        }
    }

    fun handleStorageOptionSelection(selectedUri: String){
        this.destinationDir = selectedUri
    }


    fun handleClickNegative(){
        view.runOnUiThread(Runnable {
            if(::manager.isInitialized){
                manager.cancelExporting()
            }
            view.dismissDialog()
        })
    }
}