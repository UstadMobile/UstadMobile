package com.ustadmobile.util

import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.util.Util.stopEventPropagation
import web.events.Event
import org.w3c.files.File
import web.dom.Element
import web.dom.document
import web.events.EventType
import web.timers.setTimeout
import web.uievents.*

interface OnFileAttached {
    fun onValidFileAttached(file: File)
    fun onInvalidFileAttached()
}

/**
 * Manages file selection from storage whether is br normal browsing or drag & drop,
 * It will filter out files as per mimetype specified and make sure only accepted files
 * are processed.
 */
class FileDropZoneManager(
    dropZoneId: String = "um-dropzone",
    val acceptedMimeTypesAndExtensions: List<String>
) {

    private val mimeTypeMatcher = MimeTypeMatcher(acceptedMimeTypesAndExtensions)

    private var dropZoneElement: Element? = null

    private var dropZoneInput: Element? = null

    @Suppress("UNUSED_VARIABLE")
    private var onFileInputChangedHandler :(Event) -> Unit = {
        stopEventPropagation(it)
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
        setTimeout({
            stopEventPropagation(it)
        }, 2000)
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
        dropZoneElement?.addEventListener(MouseEvent.CLICK, onFileBrowseHandler)
        dropZoneElement?.addEventListener(DragEvent.DRAG_OVER, onFileDragOverHandler)
        dropZoneElement?.addEventListener(DragEvent.DRAG_LEAVE, onFileDragLeaveHandler)
        dropZoneElement?.addEventListener(DragEvent.DROP, onFileDropHandler)
        dropZoneInput?.addEventListener(EventType("change"), onFileInputChangedHandler)
    }

    fun onDestroy(){
        onFileAttached = null
        dropZoneElement?.removeEventListener(MouseEvent.CLICK, onFileBrowseHandler)
        dropZoneElement?.removeEventListener(DragEvent.DRAG_OVER, onFileDragOverHandler)
        dropZoneElement?.removeEventListener(DragEvent.DRAG_LEAVE, onFileDragLeaveHandler)
        dropZoneElement?.removeEventListener(DragEvent.DROP, onFileDropHandler)
        dropZoneInput?.removeEventListener(EventType("change"), onFileInputChangedHandler)
        dropZoneElement = null
        dropZoneInput = null
    }
}