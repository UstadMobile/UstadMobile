package com.ustadmobile.view

import com.ustadmobile.core.controller.ContentEntryList2Presenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.determineListMode
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_SELECT_FOLDER_VISIBLE
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.umIconButton
import com.ustadmobile.mui.components.umMenu
import com.ustadmobile.mui.components.umMenuItem
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.UmProps
import com.ustadmobile.view.ext.renderAddContentEntryOptionsDialog
import com.ustadmobile.view.ext.renderContentEntryListItem
import com.ustadmobile.view.ext.umItem
import kotlinx.browser.document
import kotlinx.css.display
import org.w3c.dom.Element
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date


class ContentEntryListComponent(props: UmProps): UstadListComponent<ContentEntry,
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>(props), ContentEntryList2View{

    private var mPresenter: ContentEntryList2Presenter? = null

    private var showingEditOptions = false

    private var anchorElement: Element? = null

    override val listPresenter: UstadListPresenter<*, in ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?
        get() = mPresenter

    override val displayTypeRepo: Any?
        get() = dbRepo?.contentEntryDao

    override var editOptionVisible: Boolean = false
        get() = field
        set(value) {
            showEditOptionsMenu = value
            setState {
                field = value
            }
        }

    override var title: String? = null
        set(value) {
            ustadComponentTitle = value
            field = value
        }

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = arguments[UstadView.ARG_PARENT_ENTRY_TITLE] ?: getString(MessageID.content)
        fabManager?.text = getString(MessageID.content)
        mPresenter = ContentEntryList2Presenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        val showSelectBtn = arguments.determineListMode().toString() == ListViewMode.PICKER.toString()
                && (arguments[ARG_SELECT_FOLDER_VISIBLE]?.toBoolean() ?: true || item.leaf)
        val showStatus = arguments.determineListMode().toString() != ListViewMode.PICKER.toString()
        renderContentEntryListItem(item,systemImpl, showSelectBtn, showStatus){
            if(showSelectBtn){
                mPresenter?.onClickSelectContentEntry(item)
            }else {
                mPresenter?.onClickDownloadContentEntry(item)
            }
        }
    }

    override fun handleClickEntry(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        mPresenter?.onClickContentEntry(entry)
    }


    override fun showContentEntryAddOptions() {
        setState {
            showAddEntryOptions = true
        }
    }

    override fun RBuilder.renderAddContentOptionsDialog() {
        if(showAddEntryOptions){
            renderAddContentEntryOptionsDialog(systemImpl,
                onClickNewFolder = { mPresenter?.onClickNewFolder() },
                onClickAddFromLink = { mPresenter?.onClickImportLink() },
                onClickAddFile = { mPresenter?.onClickImportFile() },
                onDismiss = {
                    setState {
                        showAddEntryOptions = false
                    }
                })
        }
    }


    override fun showDownloadDialog(args: Map<String, String>) {
        TODO("showDownloadDialog: Not yet implemented")
    }

    override fun RBuilder.renderEditOptionMenu() {
        umItem(GridSize.cells1) {
            css{
                display = displayProperty(editOptionVisible)
            }

            umIconButton("more_vert",
                id = "more-option",
                onClick = {
                    setState {
                        showingEditOptions = true
                        anchorElement = document.getElementById("more-option")
                    }
            })
        }

        styledDiv{
            umMenu(showingEditOptions,
                anchorElement = anchorElement,
                onClose = {
                    setState {
                        showingEditOptions = false
                        anchorElement = null
                    }
                }) {

                umMenuItem(getString(MessageID.edit),
                    onClick = {
                        mPresenter?.handleClickEditFolder()
                    }
                )
                umMenuItem(getString(MessageID.show_hidden_items),
                    onClick = {
                        mPresenter?.handleClickShowHiddenItems()
                    }
                )
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }

    companion object {
       val CONTENT_ENTRY_TYPE_ICON_MAP = mapOf(
            ContentEntry.TYPE_EBOOK to "book",
            ContentEntry.TYPE_VIDEO to "smart_display",
            ContentEntry.TYPE_DOCUMENT to "description",
            ContentEntry.TYPE_ARTICLE to "article",
            ContentEntry.TYPE_COLLECTION to "collections",
            ContentEntry.TYPE_INTERACTIVE_EXERCISE to "touch_app",
            ContentEntry.TYPE_AUDIO to "audiotrack"
        )

        val CONTENT_ENTRY_TYPE_LABEL_MAP = mapOf(
            ContentEntry.TYPE_EBOOK to MessageID.ebook,
            ContentEntry.TYPE_VIDEO to MessageID.video,
            ContentEntry.TYPE_DOCUMENT to MessageID.document,
            ContentEntry.TYPE_ARTICLE to MessageID.article,
            ContentEntry.TYPE_COLLECTION to MessageID.collection,
            ContentEntry.TYPE_INTERACTIVE_EXERCISE to MessageID.interactive,
            ContentEntry.TYPE_AUDIO to MessageID.audio
        )
    }
}