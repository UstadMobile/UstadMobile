package com.ustadmobile.controller

import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.SelectFileView
import com.ustadmobile.core.view.SelectFileView.Companion.ARG_SELECTION_MODE
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.util.urlSearchParamsToMap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.w3c.files.File
import org.w3c.xhr.FormData
import org.w3c.xhr.XMLHttpRequest

@Serializable
data class FileUploadResponse(var contentEntryUid: Long, var status: Int)

class SelectFilePresenter(
    context: Any,
    arguments: Map<String, String>,
    view: SelectFileView,
    lifecycleOwner: DoorLifecycleOwner,
    di: DI): UstadEditPresenter<SelectFileView, Any>(context, arguments, view, di, lifecycleOwner){

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    private var parentEntryUid: Long = 0L

    override fun onCreate(savedState: Map<String, String>?){
        parentEntryUid = arguments[UstadView.ARG_PARENT_ENTRY_UID]?.toLong() ?: 0L
        view.acceptedMimeTypes = urlSearchParamsToMap()[ARG_SELECTION_MODE].toString().split(";").map{
            //Add .(dot) to file extensions and format for JS
            if(it.indexOf("/") == -1) ".${it}" else it.replace(" ","+")
        }.toList()
    }

    override fun handleClickSave(entity: Any) {
        val selectedFiles = entity.asDynamic() as List<File>
        if(selectedFiles.isNullOrEmpty()){
            view.noFileSelectedError = systemImpl.getString(MessageID.file_required_prompt, this)
            return
        }
        view.loading = true
        val fileToUpload = selectedFiles.first()
        val formData = FormData()
        formData.append("file", fileToUpload)
        val endPoint = "${accountManager.activeEndpoint.url}contentupload/upload"
        val request = XMLHttpRequest()
        request.open("POST", endPoint)
        request.send(formData)
        request.onreadystatechange = {
            if(request.readyState.toInt() == 4){
                val response = safeParse(di, FileUploadResponse.serializer(), request.responseText)
                if(response.contentEntryUid > 0){
                    //Wait for replication engine to replicate before processing the content entry
                    db.contentEntryDao.findLiveContentEntry(response.contentEntryUid).observe(lifecycleOwner){ entry ->
                        if(entry != null){
                            val args = mutableMapOf(
                                UstadView.ARG_ENTITY_UID to entry.contentEntryUid.toString(),
                                UstadView.ARG_CLAZZUID to "0",
                                UstadView.ARG_PARENT_ENTRY_TITLE to entry.title,
                                )
                            val contentEntryJoin = ContentEntryParentChildJoin().apply {
                                cepcjChildContentEntryUid = entry.contentEntryUid
                                cepcjParentContentEntryUid = parentEntryUid
                            }

                            //Make sure entry has it's container ready before navigation
                            db.contentEntryDao.findEntryWithContainerByEntryIdLive(entry.contentEntryUid).observe(lifecycleOwner){
                                if(it != null){
                                    view.loading = false
                                    systemImpl.go(ContentEntryDetailView.VIEW_NAME, args, this)
                                }
                            }
                            GlobalScope.launch{
                                repo.contentEntryParentChildJoinDao.insertAsync(contentEntryJoin)
                            }
                        }
                    }
                }else {
                    view.loading = false
                    view.unSupportedFileError = systemImpl.getString(
                        MessageID.import_link_content_not_supported, this)
                }
            }
        }
    }
}