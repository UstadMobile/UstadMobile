package com.ustadmobile.controller

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.SelectFileView
import com.ustadmobile.core.view.SelectFileView.Companion.ARG_SELECTION_MODE
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.util.urlSearchParamsToMap
import kotlinx.coroutines.Dispatchers
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

    override fun onCreate(savedState: Map<String, String>?){
        view.acceptedMimeTypes = urlSearchParamsToMap()[ARG_SELECTION_MODE].toString().split(";")
    }


    override fun handleClickSave(entity: Any) {
        val selectedFiles = entity.asDynamic() as Array<dynamic>
        if(selectedFiles.isNullOrEmpty()){
            view.noFileSelectedError = systemImpl.getString(MessageID.file_required_prompt, this)
            return
        }
        view.loading = true
        val fileToUpload = selectedFiles.first().file as File
        val formData = FormData()
        formData.append("file", fileToUpload)
        val endPoint = "${accountManager.activeEndpoint.url}contentupload/upload"
        val request = XMLHttpRequest()
        request.open("POST", endPoint)
        request.send(formData)
        request.onreadystatechange = {
            if(request.readyState.toInt() == 4){
                view.loading = false
                val response = safeParse(di, FileUploadResponse.serializer(), request.responseText)
                GlobalScope.launch(Dispatchers.Main) {
                    if(response.contentEntryUid > 0){
                        val parentEntryUid = arguments[UstadView.ARG_PARENT_ENTRY_UID]
                        val leafContent = arguments[UstadView.ARG_LEAF]?.toBoolean() ?: false
                        val contentEntry = ContentEntryWithLanguage().apply {
                            contentEntryUid = response.contentEntryUid
                            leaf = leafContent
                        }
                        val metadata = safeStringify(di, MetadataResult.serializer(),
                            MetadataResult(entry = contentEntry, pluginId = -1)
                        )
                        val entry = repo.contentEntryDao.findByUidAsync(response.contentEntryUid)
                        val args = mutableMapOf(
                            ContentEntryEdit2View.ARG_IMPORTED_METADATA to metadata,
                            UstadView.ARG_CONTENT_ENTRY_UID to response.contentEntryUid.toString(),
                            UstadView.ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                            UstadView.ARG_POPUPTO_ON_FINISH to ContentEntryList2View.VIEW_NAME_HOME,
                            UstadView.ARG_LEAF to arguments[UstadView.ARG_LEAF].toString()
                        )
                        systemImpl.go(ContentEntryEdit2View.VIEW_NAME, args, this)
                    }else {
                        view.unSupportedFileError = systemImpl.getString(
                            MessageID.import_link_content_not_supported, this)
                    }
                }
            }
        }
    }
}