package com.ustadmobile.view

import com.ustadmobile.core.controller.ContentEntryList2Presenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.determineListMode
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_SELECT_FOLDER_VISIBLE
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.umEntityAvatar
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.browser.document
import kotlinx.css.*
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


    override fun onCreateView() {
        super.onCreateView()
        fabManager?.text = getString(MessageID.content)
        mPresenter = ContentEntryList2Presenter(this, arguments, this,di,this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        val showSelectBtn = arguments.determineListMode().toString() == ListViewMode.PICKER.toString() &&
                (arguments[ARG_SELECT_FOLDER_VISIBLE]?.toBoolean() == true || item.leaf)

        umGridContainer(GridSpacing.spacing7) {
            umItem(GridSize.cells4, GridSize.cells3){
                umEntityAvatar(item.thumbnailUrl,
                    if(item.leaf) Util.ASSET_BOOK else Util.ASSET_FOLDER,
                    showIcon = false,
                    className = "${StyleManager.name}-entityThumbnailClass")
            }

            umItem(GridSize.cells8, GridSize.cells9){
                umItem(GridSize.cells12){
                    umTypography(item.title,
                        variant = TypographyVariant.h6){
                        css {
                            +alignTextToStart
                            marginBottom = LinearDimension("10px")
                        }
                    }
                }

                umItem(GridSize.cells12){
                    umTypography(item.description,
                        variant = TypographyVariant.body1,
                        paragraph = true){
                        css(alignTextToStart)
                    }
                }

                umItem(GridSize.cells12){
                    umGridContainer(spacing= GridSpacing.spacing1){
                        css{
                            display = displayProperty(item.leaf, true)
                        }
                        val messageId = CONTENT_ENTRY_TYPE_LABEL_MAP[item.contentTypeFlag] ?: MessageID.untitled
                        val icon = CONTENT_ENTRY_TYPE_ICON_MAP[item.contentTypeFlag] ?: ""
                        umItem(GridSize.cells2, GridSize.cells1) {
                            umAvatar(className = "${StyleManager.name}-contentEntryListContentAvatarClass") {
                                umIcon(icon, className= "${StyleManager.name}-contentEntryListContentTyeIconClass"){
                                    css{marginTop = 4.px}
                                }
                            }
                        }

                        umItem(GridSize.cells8, GridSize.cells9) {
                            umTypography(getString(messageId),
                                variant = TypographyVariant.body2,
                                gutterBottom = true)
                        }

                        umItem(GridSize.cells2){
                            umButton(getString(MessageID.select_item).format(""),
                                variant = ButtonVariant.outlined,
                                color = UMColor.secondary,
                                onClick = {
                                    it.stopPropagation()
                                    mPresenter?.onClickSelectContentEntry(item)
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
        mPresenter?.onClickContentEntry(entry)
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
                    mPresenter?.onClickNewFolder()
                },
                PopUpOptionItem("link",MessageID.add_using_link,
                    MessageID.add_link_description) {
                    mPresenter?.onClickImportLink()
                },
                PopUpOptionItem("collections",MessageID.add_from_gallery,
                    MessageID.add_gallery_description) {
                    mPresenter?.onClickImportGallery()
                },
                PopUpOptionItem("note_add",MessageID.add_file,
                    MessageID.add_file_description) {
                    mPresenter?.onClickImportFile()
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
        umItem(GridSize.cells1) {
            css{
                display = displayProperty(editOptionVisible)
            }

            umIconButton("more_vert",
                onClick = {
                    console.log(it.currentTarget)
                    setState {
                        showingEditOptions = true
                        anchorElement = document.getElementById("more-option")
                    }
            }){
                attrs.id = "more-option"
            }
        }

        styledDiv{
            umMenu(showingEditOptions,
                anchorElement = anchorElement,
                onClose = {
                    setState {
                        showingEditOptions = false;
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