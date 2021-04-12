package com.ustadmobile.view

import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.list.mListItemText
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.variant
import com.ustadmobile.core.controller.ContentEntryList2Presenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.VIEW_NAME
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.model.statemanager.GlobalStateSlice
import com.ustadmobile.model.statemanager.AppBarState
import com.ustadmobile.util.CssStyleManager.entryListItemContainer
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.util.StateManager
import kotlinx.browser.window
import kotlinx.css.*
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.span
import styled.css
import styled.styledDiv
import styled.styledH1
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
    }

    override fun onComponentRefreshed() {
        console.log("Component refreshed")
    }

    override fun RBuilder.renderListItem(item: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        styledDiv {
            css(entryListItemContainer)
            styledImg {
                css { marginRight = 20.px }
                attrs{
                    src = item.thumbnailUrl.toString()
                    width = "100px"
                }}
            styledDiv {
                mTypography(item.title,variant = MTypographyVariant.h6){
                    css {
                        marginBottom = LinearDimension("10px")
                    }
                }
                mTypography(item.description, variant = MTypographyVariant.body2)
            }
        }
    }

    override fun RBuilder.renderHeaderView() {
        styledDiv {  }
    }

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
            ContentEntry.TYPE_EBOOK to "",
            ContentEntry.TYPE_VIDEO to "",
            ContentEntry.TYPE_DOCUMENT to "",
            ContentEntry.TYPE_ARTICLE to "",
            ContentEntry.TYPE_COLLECTION to "",
            ContentEntry.TYPE_INTERACTIVE_EXERCISE to "",
            ContentEntry.TYPE_AUDIO to ""
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