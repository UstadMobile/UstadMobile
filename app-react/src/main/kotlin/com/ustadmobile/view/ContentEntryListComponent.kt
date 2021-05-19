package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.dialog.mDialog
import com.ccfraser.muirwik.components.dialog.mDialogActions
import com.ccfraser.muirwik.components.dialog.mDialogContent
import com.ccfraser.muirwik.components.list.mList
import com.ccfraser.muirwik.components.list.mListItem
import com.ccfraser.muirwik.components.list.mListItemAvatar
import com.ccfraser.muirwik.components.list.mListItemText
import com.ccfraser.muirwik.components.menu.mMenu
import com.ccfraser.muirwik.components.menu.mMenuItem
import com.ustadmobile.core.controller.ContentEntryList2Presenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CONTENT_FILTER
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_WEB_PLATFORM
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.util.CssStyleManager
import com.ustadmobile.util.CssStyleManager.alignTextToStart
import com.ustadmobile.util.CssStyleManager.entryListItemContainer
import com.ustadmobile.util.CssStyleManager.entryListItemImageContainer
import com.ustadmobile.util.CssStyleManager.entryListItemInfo
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.view.ext.renderEntryThumbnailImg
import kotlinx.css.*
import org.w3c.dom.Node
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv
import styled.styledImg

interface EntryListProps: RProps{
    var args: Map<String,String>
}


class ContentEntryListComponent(props: EntryListProps): UstadListViewComponent<ContentEntry,
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>(props), ContentEntryList2View{

    private lateinit var mPresenter: ContentEntryList2Presenter

    private var showAddEntryOptions = false

    private var mParentEntryUid:Long = 0

    private var showingEditOptions = false

    private var anchorElement: Node? = null

    override val listPresenter: UstadListPresenter<*, in ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?
        get() = mPresenter

    override val displayTypeRepo: Any?
        get() = dbRepo?.contentEntryDao

    override val viewName: String
        get() = ContentEntryList2View.VIEW_NAME

    override var editOptionVisible: Boolean = false
        get() = field
        set(value) {
            showEditOptionsMenu = value
            setState { field = value }
        }


    override fun onComponentReady() {
        fabState.label = systemImpl.getString(MessageID.content, this)
        val args = getArgs(mapOf(ARG_WEB_PLATFORM to true.toString(),
            ARG_CONTENT_FILTER to ARG_LIBRARIES_CONTENT))
        mPresenter = ContentEntryList2Presenter(this, args, this,di,this)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        styledDiv {
            css(entryListItemContainer)
            styledDiv {
                css { +entryListItemImageContainer }
                styledImg {
                    css{
                        width = LinearDimension("100%")
                        height = LinearDimension("100%")
                    }
                    attrs{ renderEntryThumbnailImg(item) }
                }
            }
            styledDiv {
                css(entryListItemInfo)
                mTypography(item.title,variant = MTypographyVariant.h6){
                    css {
                        +alignTextToStart
                        marginBottom = LinearDimension("10px")
                    }
                }
                mTypography(item.description, variant = MTypographyVariant.body1, paragraph = true){
                    css(alignTextToStart)
                }

                mGridContainer(spacing= MGridSpacing.spacing1){
                    css{
                        display = if(item.leaf) Display.flex else Display.none
                    }
                    val messageId = CONTENT_ENTRY_TYPE_LABEL_MAP[item.contentTypeFlag]?:MessageID.untitled
                    val icon = CONTENT_ENTRY_TYPE_ICON_MAP[item.contentTypeFlag]?:""
                    mGridItem {
                        mAvatar(className = "${CssStyleManager.name}-contentEntryListAvatar") {
                            mIcon(icon, className= "${CssStyleManager.name}-contentEntryListIcon"){
                                css{marginTop = 4.px}
                            }
                        }
                    }

                    mGridItem {
                        mTypography(systemImpl.getString(messageId, this),
                            variant = MTypographyVariant.body2, gutterBottom = true)
                    }
                }
            }
        }
    }

    override fun handleClickEntry(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        mPresenter.onClickContentEntry(entry)
    }

    override fun showContentEntryAddOptions(parentEntryUid: Long) {
        setState {
            mParentEntryUid = parentEntryUid
            showAddEntryOptions = true
        }
    }

    override fun RBuilder.renderAddEntryOptions() {
        mDialog(showAddEntryOptions, onClose = { _, _ -> setState { showAddEntryOptions = false}}) {
            mDialogContent {
                css { width = 25.pc }
                styledDiv {
                    css {
                        width = LinearDimension("100%")
                    }
                    mList {
                        mListItem(button = true, onClick = {
                            handleAddEntryOptionClicked(false) }) {
                            mListItemAvatar {
                                mAvatar {
                                    mIcon("create_new_folder")
                                }
                            }
                            mListItemText(primary = systemImpl.getString(
                                MessageID.content_editor_create_new_category,this))
                        }

                        mListItem(button = true, onClick = {
                            handleAddEntryOptionClicked(true) }) {
                            mListItemAvatar {
                                mAvatar {
                                    mIcon("exit_to_app")
                                }
                            }
                            mListItemText(primary = systemImpl.getString(
                                MessageID.import_content,this))
                        }
                    }
                }
            }

            mDialogActions {
                mButton(systemImpl.getString(MessageID.cancel,this), color = MColor.primary, onClick = { setState {showAddEntryOptions = false}})
            }
        }
    }

    private fun handleAddEntryOptionClicked(contentType: Boolean){
        systemImpl.go(ContentEntryEdit2View.VIEW_NAME,mapOf(
            ARG_PARENT_ENTRY_UID to mParentEntryUid.toString(),
            UstadView.ARG_LEAF to contentType.toString()),this)
    }

    override fun showDownloadDialog(args: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun showMoveEntriesFolderPicker(selectedContentEntryParentChildJoinUids: String) {
        //implement moving entries from one folder to another
    }


    override fun RBuilder.renderEditOptionMenu() {
        mGridItem {
            css{
                display = if(editOptionVisible) Display.block else Display.none
            }
            mIconButton("more_vert", color = MColor.default, onClick = {
                val target =  it.currentTarget
                setState {
                    showingEditOptions = true; anchorElement = target.asDynamic()
                }
            })
        }

        styledDiv{
            mMenu(showingEditOptions, anchorElement = anchorElement,
                onClose = { _, _ -> setState { showingEditOptions = false; anchorElement = null}}) {
                mMenuItem(systemImpl.getString(MessageID.edit, this), onClick = {
                    mPresenter.handleClickEditFolder()
                })
                mMenuItem(systemImpl.getString(MessageID.show_hidden_items, this), onClick = {
                    mPresenter.handleClickShowHiddenItems()
                })
            }
        }
    }


    override fun componentWillUnmount() {
        super.componentWillUnmount()
        mPresenter.onDestroy()
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