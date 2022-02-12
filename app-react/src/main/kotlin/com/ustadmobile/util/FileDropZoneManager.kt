package com.ustadmobile.util

import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.util.Util.stopEventPropagation
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import org.w3c.files.File

interface OnFileAttached {
    fun onValidFileAttached(file: File)
    fun onInvalidFileAttached()
}

class FileDropZoneManager(
    dropZoneId: String = "um-dropzone",
    val acceptedMimeTypesAndExtensions: List<String>) {

    private val mimeTypeMatcher = MimeTypeMatcher(acceptedMimeTypesAndExtensions)

    private var dropZoneElement: Element? = null

    private var dropZoneInput: Element? = null

    @Suppress("UNUSED_VARIABLE")
    private var onFileInputChangedHandler :(Event) -> Unit = {
        handleSelectedFile(it.asDynamic().target.files[0] as File)
    }

    private var onFileDragOverHandler :(Event) -> Unit = {
        stopEventPropagation(it)
        dropZoneElement?.classList?.remove("${StyleManager.name}-dropZoneAreaSuccess")
        dropZoneElement?.classList?.remove("${StyleManager.name}-dropZoneAreaError")
        dropZoneElement?.classList?.add("${StyleManager.name}-dropZoneAreaActive")
    }

    private var onFileDragLeaveHandler :(Event) -> Unit = {
        stopEventPropagation(it)
        dropZoneElement?.classList?.remove("${StyleManager.name}-dropZoneAreaActive")
    }

    private var onFileDropHandler :(Event) -> Unit = {
        stopEventPropagation(it)
        handleSelectedFile(it.asDynamic().dataTransfer.files[0] as File)
    }

    private var onFileBrowseHandler :(Event) -> Unit = {
        dropZoneElement?.classList?.add("${StyleManager.name}-dropZoneAreaActive")
        dropZoneInput?.asDynamic().click()
    }

    var onFileAttached: OnFileAttached? = null
        set(value) {
            field = value
        }

    private fun handleSelectedFile(file: File){
        val extOrMimeType = file.type.ifEmpty {
            val ext = UMFileUtil.getExtension(file.name)
            if(ext != null) ".${ext}" else null
        }
        val validFile = mimeTypeMatcher.match(extOrMimeType)
        if(validFile){
            onFileAttached?.onValidFileAttached(file)
        }else {
            onFileAttached?.onInvalidFileAttached()
        }
        dropZoneElement?.classList?.add(StyleManager.name + "-dropZoneArea${if(validFile)
            "Success" else "Error"}")
    }

    init {
        dropZoneElement = document.getElementById(dropZoneId)
        dropZoneInput = dropZoneElement?.querySelector("input")
        dropZoneElement?.addEventListener("click", onFileBrowseHandler)
        dropZoneElement?.addEventListener("dragover", onFileDragOverHandler)
        dropZoneElement?.addEventListener("dragleave", onFileDragLeaveHandler)
        dropZoneElement?.addEventListener("drop", onFileDropHandler)
        dropZoneInput?.addEventListener("change", onFileInputChangedHandler)
    }

    fun onDestroy(){
        onFileAttached = null
        dropZoneElement?.removeEventListener("click", onFileBrowseHandler)
        dropZoneElement?.removeEventListener("dragover", onFileDragOverHandler)
        dropZoneElement?.removeEventListener("dragleave", onFileDragLeaveHandler)
        dropZoneElement?.removeEventListener("drop", onFileDropHandler)
        dropZoneInput?.removeEventListener("change", onFileInputChangedHandler)
        dropZoneElement = null
        dropZoneInput = null
    }
}