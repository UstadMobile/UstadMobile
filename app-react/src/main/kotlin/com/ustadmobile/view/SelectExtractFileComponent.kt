package com.ustadmobile.view

import com.ustadmobile.core.controller.SelectExtractFilePresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.SelectExtractFileView
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.themeContext
import com.ustadmobile.mui.components.umIcon
import com.ustadmobile.util.*
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import io.github.aakira.napier.Napier
import kotlinx.css.Visibility
import kotlinx.css.minHeight
import kotlinx.css.vh
import kotlinx.css.visibility
import kotlinx.html.InputType
import kotlinx.html.id
import org.w3c.dom.url.URL
import org.w3c.files.File
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import styled.styledHeader
import styled.styledInput

class SelectExtractFileComponent(
    props: UmProps
): UstadBaseComponent<UmProps, UmState>(props), SelectExtractFileView  {

    private var mPresenter: SelectExtractFilePresenter? = null

    private var fileDropZoneManager: FileDropZoneManager? = null

    private var dropZoneText: String = getString(MessageID.drag_and_drop_or_click_to_add_file)

    override var acceptedMimeTypes: List<String> = listOf("*/")
        set(value) {
            setState {
                field = value
            }
        }

    override var noFileSelectedError: String? = null
        set(value) {

        }

    override var unSupportedFileError: String? = null
        set(value) {

        }
    override var fieldsEnabled: Boolean = true
        set(value) {

        }
    override var entity: Any? = null
        set(value) {

        }

    private var selectedFiles: List<File> = listOf()
        get() = field
        set(value) {
            field = value
            noFileSelectedError = null
        }

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = SelectExtractFilePresenter(this, arguments, this,
            di)
        fabManager?.icon = "upload"
        fabManager?.text = getString(MessageID.upload)
        fabManager?.visible = true
        fabManager?.onClickListener = {
            val fileObjectUrl = selectedFiles.firstOrNull()?.let {
                URL.Companion.createObjectURL(it)
            }
            Napier.d { "SelectExtractFileComponent: objectUrl = $fileObjectUrl" }
            mPresenter?.handleUriSelected(fileObjectUrl, selectedFiles.firstOrNull()?.name)
        }

        mPresenter?.onCreate(mapOf())

        fileDropZoneManager = FileDropZoneManager(acceptedMimeTypesAndExtensions = listOf("*/*"))
        fileDropZoneManager?.onFileAttached = object: OnFileAttached {
            override fun onValidFileAttached(file: File) {
                setState {
                    selectedFiles = listOf(file)
                    dropZoneText = getString(MessageID.selected_file_summary).format(
                        file.name, UMFileUtil.formatFileSize(file.size.toString().toLong())
                    )
                }
            }

            override fun onInvalidFileAttached() {
                setState {
                    dropZoneText = getString(MessageID.drag_and_drop_or_click_to_add_file)
                    unSupportedFileError = getString(MessageID.import_link_content_not_supported)
                }
            }
        }
    }

    override fun RBuilder.render() {
        themeContext.Consumer { _ ->
            umGridContainer {
                umItem(GridSize.cells12) {
                    css{
                        +StyleManager.centerContainer
                        minHeight = 92.vh
                    }
                    styledDiv {
                        css(StyleManager.dropZoneArea)
                        attrs.id = "um-dropzone"
                        umIcon("cloud_upload", className = "${StyleManager.name}-dropZoneIcon")
                        styledHeader {
                            css(StyleManager.dropZoneTxt)
                            +dropZoneText
                        }
                        styledInput {
                            css{
                                visibility = Visibility.hidden
                            }
                            attrs.type = InputType.file
                            attrs.accept = acceptedMimeTypes.joinToString(",")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        entity = null
    }
}