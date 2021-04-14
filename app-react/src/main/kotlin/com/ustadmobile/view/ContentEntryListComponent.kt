package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ustadmobile.core.controller.ContentEntryList2Presenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.model.statemanager.AppBarState
import com.ustadmobile.util.CssStyleManager.entryListItemContainer
import com.ustadmobile.util.CssStyleManager.entryListItemImage
import com.ustadmobile.util.CssStyleManager.entryListItemInfo
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.util.StateManager
import kotlinx.browser.window
import kotlinx.css.*
import react.RBuilder
import react.RProps
import styled.css
import styled.styledDiv
import styled.styledImg

interface EntryListProps: RProps


class ContentEntryListComponent(props: EntryListProps): UstadListViewComponent<ContentEntry,
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>(props), ContentEntryList2View{

    private lateinit var mPresenter: ContentEntryList2Presenter

    override val listPresenter: UstadListPresenter<*, in ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?
        get() = mPresenter

    override fun componentDidMount() {
        super.componentDidMount()
        mPresenter = ContentEntryList2Presenter(this,
            getArgs(), this,di,this)
        //mPresenter.onCreate(mapOf())

        //remove this
        window.setTimeout({
            listFilterOptionChips = listOf(ListFilterIdOption("Filter 1", 0),
                ListFilterIdOption("Filter 2", 1))
        }, 1000)
    }

    override fun RBuilder.renderListItem(item: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        styledDiv {
            css(entryListItemContainer)
            styledImg {
                css { +entryListItemImage }
                attrs{
                    src = item.thumbnailUrl.toString()
                }}
            styledDiv {
                css{+entryListItemInfo}
                mTypography(item.title,variant = MTypographyVariant.h6){
                    css {
                        marginBottom = LinearDimension("10px")
                    }
                }
                mTypography(item.description, variant = MTypographyVariant.body1, paragraph = true)

                mGridContainer(spacing= MGridSpacing.spacing1){
                    val messageId = CONTENT_ENTRY_TYPE_LABEL_MAP[item.contentTypeFlag]?:0
                    val icon = CONTENT_ENTRY_TYPE_ICON_MAP[item.contentTypeFlag]?:""
                    mGridItem {
                        mIcon(icon, fontSize = MIconFontSize.small)
                    }

                    mGridItem {
                        mTypography(
                            systemImpl.getString(messageId, this),
                            variant = MTypographyVariant.body2, gutterBottom = true)
                    }
                }
            }
        }
    }

    override fun RBuilder.renderHeaderView() {}

    override fun handleClickEntry(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        mPresenter.onClickContentEntry(entry)
    }


    override fun componentWillUnmount() {
        super.componentWillUnmount()
        mPresenter.onDestroy()
    }

    override val displayTypeRepo: Any?
        get() = TODO("Not yet implemented")


    override fun styleList(): RuleSet? {
        return null
    }

    override fun getData(offset: Int, limit: Int): List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> {
        return (window.asDynamic().entries as List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>)
            .filter {it.entryId.toString() == getArgs()[ARG_PARENT_ENTRY_UID] }
    }


    override fun showContentEntryAddOptions(parentEntryUid: Long) {
        TODO("Not yet implemented")
    }

    override fun showDownloadDialog(args: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun showMoveEntriesFolderPicker(selectedContentEntryParentChildJoinUids: String) {
        TODO("Not yet implemented")
    }

    override var title: String? = null
        get() = field
        set(value) {
            field = value
            StateManager.dispatch(AppBarState(title = value))
        }

    override var editOptionVisible: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override var sortOptions: List<MessageIdOption>?
        get() = TODO("Not yet implemented")
        set(value) {}

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