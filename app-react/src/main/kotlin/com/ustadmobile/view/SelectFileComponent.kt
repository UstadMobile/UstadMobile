package com.ustadmobile.view

import com.ustadmobile.core.controller.SelectFilePresenterCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.UMFileUtil.formatFileSize
import com.ustadmobile.core.view.SelectFileView
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.themeContext
import com.ustadmobile.mui.components.umIcon
import com.ustadmobile.util.*
import com.ustadmobile.util.StyleManager.dropZoneArea
import com.ustadmobile.util.StyleManager.dropZoneTxt
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.browser.window
import kotlinx.css.Visibility
import kotlinx.css.minHeight
import kotlinx.css.vh
import kotlinx.css.visibility
import kotlinx.html.InputType
import kotlinx.html.id
import org.w3c.files.File
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import styled.styledHeader
import styled.styledInput


class SelectFileComponent(props: UmProps): UstadBaseComponent<UmProps, UmState>(props), SelectFileView {

    private lateinit var mPresenter: SelectFilePresenterCommon

    private var dropZoneText: String = getString(MessageID.drag_and_drop_or_click_to_add_file)

    private var fileDropZoneManager: FileDropZoneManager? = null

    private var selectedFiles: List<File> = listOf()
        get() = field
        set(value) {
            field = value
            noFileSelectedError = null
        }

    override var acceptedMimeTypes: List<String> = listOf()
        get() = field
        set(value) {
            setState {
                field = value
            }
            //Delay for UI to fully render
            window.setTimeout({
                fileDropZoneManager = FileDropZoneManager(acceptedMimeTypesAndExtensions = value)
                fileDropZoneManager?.onFileAttached = object: OnFileAttached {
                    override fun onValidFileAttached(file: File) {
                       setState {
                           selectedFiles = listOf(file)
                           dropZoneText = getString(MessageID.selected_file_summary).format(
                               file.name, formatFileSize(file.size.toString().toLong()))
                       }
                    }

                    override fun onInvalidFileAttached() {
                        setState {
                            dropZoneText = getString(MessageID.drag_and_drop_or_click_to_add_file)
                            unSupportedFileError = getString(MessageID.import_link_content_not_supported)
                        }
                    }
                }
            }, MIN_STATE_CHANGE_DELAY_TIME)
        }

    override var noFileSelectedError: String? = null
        get() = field
        set(value) {
            field = value
            if(value != null){
                showSnackBar(value)
            }
        }

    override var unSupportedFileError: String? = null
        get() = field
        set(value) {
            field = value
            if(value != null){
                showSnackBar(value)
            }
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    override var entity: Any? = null
        get() = selectedFiles
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = SelectFilePresenterCommon(this, arguments, this,  di)
        fabManager?.icon = "upload"
        fabManager?.text = getString(MessageID.upload)
        fabManager?.visible = true
        fabManager?.onClickListener = {
            mPresenter.handleUriSelected(selectedFiles.firstOrNull()?.toDoorUri()?.toString())
        }
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        if(acceptedMimeTypes.isNotEmpty()){
            themeContext.Consumer { _ ->
                umGridContainer {
                    umItem(GridSize.cells12) {
                        css{
                            +StyleManager.centerContainer
                            minHeight = 92.vh
                        }
                        styledDiv {
                            css(dropZoneArea)
                            attrs.id = "um-dropzone"
                            umIcon("cloud_upload", className = "${StyleManager.name}-dropZoneIcon")
                            styledHeader {
                                css(dropZoneTxt)
                                +dropZoneText
                            }
                            styledInput {
                                css{
                                    visibility = Visibility.hidden
                                }
                                attrs.type = InputType.file
                                attrs.id = "um-file-select"
                                attrs.accept = acceptedMimeTypes.joinToString(",")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        noFileSelectedError = null
        unSupportedFileError = null
        fileDropZoneManager?.onDestroy()
        fileDropZoneManager = null
        entity = null
    }
}