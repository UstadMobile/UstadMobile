package com.ustadmobile.view

import com.ustadmobile.controller.SelectFilePresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.SelectFileView
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.themeContext
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.get
import react.RBuilder
import react.setState
import styled.css


class SelectFileComponent(props: UmProps): UstadBaseComponent<UmProps, UmState>(props), SelectFileView {

    private lateinit var mPresenter: SelectFilePresenter

    override val viewNames: List<String>
        get() = listOf(SelectFileView.VIEW_NAME)

    private var selectedFiles: Array<Any> = arrayOf()
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

    private var dropZoneText: String = getString(MessageID.import_content)

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = SelectFilePresenter(this, arguments, this, this, di)
        fabManager?.icon = "upload"
        fabManager?.text = getString(MessageID.submit)
        fabManager?.visible = true
        fabManager?.onClickListener = {
            mPresenter.handleClickSave(selectedFiles)
        }
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        if(acceptedMimeTypes.isNotEmpty()){
            themeContext.Consumer { _ ->
                umGridContainer {
                    umItem(GridSize.cells12) {
                        css(StyleManager.centerContainer)
                        umDropZone(
                            acceptedMimeTypes.toTypedArray(),
                            dropzoneText = dropZoneText,
                            dropzoneClass = "${StyleManager.name}-filedDropClass"
                        ) {
                            window.setTimeout({
                                val element =
                                    document.getElementsByClassName("MuiDropzoneArea-icon")?.get(0)
                                element?.classList?.add("${StyleManager.name}-dropZoneIconClass")
                            }, STATE_CHANGE_DELAY)
                            attrs.onAdd = {
                                selectedFiles = it
                                if(selectedFiles.isNotEmpty()){
                                    val file = (it as Array<dynamic>).first().file
                                    setState {
                                        dropZoneText = "${file.name} -" +
                                                " ${UMFileUtil.formatFileSize(file.size.toString().toLong())}"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}