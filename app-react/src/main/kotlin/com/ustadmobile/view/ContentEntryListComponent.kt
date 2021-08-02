package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.menu.mMenu
import com.ccfraser.muirwik.components.menu.mMenuItem
import com.ustadmobile.core.controller.ContentEntryList2Presenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.determineListMode
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_SELECT_FOLDER_VISIBLE
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView.Companion.ARG_WEB_PLATFORM
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.Util
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.umEntityAvatar
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.*
import org.w3c.dom.Node
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date


class ContentEntryListComponent(props: RProps): UstadListComponent<ContentEntry,
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>(props), ContentEntryList2View{

    private lateinit var mPresenter: ContentEntryList2Presenter

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
            setState {
                field = value
            }
        }


    override fun onCreate() {
        super.onCreate()
        fabManager?.text = getString(MessageID.content)
        mPresenter = ContentEntryList2Presenter(this, arguments, this,di,this)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        val showSelectBtn = arguments.determineListMode().toString() == ListViewMode.PICKER.toString() &&
                (arguments[ARG_SELECT_FOLDER_VISIBLE]?.toBoolean() == true || item.leaf)

        umGridContainer(MGridSpacing.spacing7) {
            umItem(MGridSize.cells4, MGridSize.cells3){
                umEntityAvatar(item.thumbnailUrl,
                    if(item.leaf) Util.ASSET_BOOK else Util.ASSET_FOLDER,
                    showIcon = false,
                    className = "${StyleManager.name}-entityThumbnailClass")
            }

            umItem(MGridSize.cells8, MGridSize.cells9){
                umItem(MGridSize.cells12){
                    mTypography(item.title,variant = MTypographyVariant.h6,
                        color = MTypographyColor.textPrimary){
                        css {
                            +alignTextToStart
                            marginBottom = LinearDimension("10px")
                        }
                    }
                }

                umItem(MGridSize.cells12){
                    mTypography(item.description, variant = MTypographyVariant.body1,
                        paragraph = true, color = MTypographyColor.textPrimary){
                        css(alignTextToStart)
                    }
                }

                umItem(MGridSize.cells12){
                    mGridContainer(spacing= MGridSpacing.spacing1){
                        css{
                            display = displayProperty(item.leaf, true)
                        }
                        val messageId = CONTENT_ENTRY_TYPE_LABEL_MAP[item.contentTypeFlag] ?: MessageID.untitled
                        val icon = CONTENT_ENTRY_TYPE_ICON_MAP[item.contentTypeFlag] ?: ""
                        umItem(MGridSize.cells2, MGridSize.cells1) {
                            mAvatar(className = "${StyleManager.name}-contentEntryListContentAvatarClass") {
                                mIcon(icon, className= "${StyleManager.name}-contentEntryListContentTyeIconClass"){
                                    css{marginTop = 4.px}
                                }
                            }
                        }

                        umItem(MGridSize.cells8, MGridSize.cells9) {
                            mTypography(getString(messageId), variant = MTypographyVariant.body2, gutterBottom = true)
                        }

                        umItem(MGridSize.cells2){
                            mButton(getString(MessageID.select_item).format(""),
                                variant = MButtonVariant.outlined,
                                color = MColor.primary,
                                onClick = {
                                    it.stopPropagation()
                                    mPresenter.onClickSelectContentEntry(item)
                                }){
                                css {
                                    display = displayProperty(showSelectBtn)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun handleClickEntry(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        mPresenter.onClickContentEntry(entry)
    }

    override fun onFabClicked() {
        setState {
            showAddEntryOptions = true
        }
    }

    override fun showContentEntryAddOptions() {
        setState {
            showAddEntryOptions = true
        }
    }

    override fun RBuilder.renderAddEntryOptionsDialog() {

        if(showAddEntryOptions){
            val options = mutableListOf(
                PopUpOptionItem("create_new_folder",
                    MessageID.content_editor_create_new_category) {
                    mPresenter.onClickNewFolder()
                },
                PopUpOptionItem("link",MessageID.add_using_link,
                    MessageID.add_link_description) {
                    mPresenter.onClickImportLink()
                },
                PopUpOptionItem("collections",MessageID.add_from_gallery,
                    MessageID.add_gallery_description) {
                    mPresenter.onClickImportGallery()
                },
                PopUpOptionItem("note_add",MessageID.add_file,
                    MessageID.add_file_description) {
                    mPresenter.onClickImportFile()
                }
            )

            renderChoices(systemImpl,options, Date().getTime().toLong()){
                setState {
                    showAddEntryOptions = false
                }
            }
        }
    }


    override fun showDownloadDialog(args: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun RBuilder.renderEditOptionMenu() {
        mGridItem {
            css{
                display = displayProperty(editOptionVisible)
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
                mMenuItem(getString(MessageID.edit), onClick = {
                    mPresenter.handleClickEditFolder()
                })
                mMenuItem(getString(MessageID.show_hidden_items), onClick = {
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