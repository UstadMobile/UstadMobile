package com.ustadmobile.view

import com.ustadmobile.core.controller.ContentEntryList2Presenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.util.UmReactUtil.queryParams
import react.RBuilder
import react.RProps
import styled.styledDiv

interface EntryListProps: RProps


class ContentEntryListComponent(props: EntryListProps): UstadListViewComponent<ContentEntry,
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>(props), ContentEntryList2View{

    private lateinit var mPresenter: ContentEntryList2Presenter

    override val listPresenter: UstadListPresenter<*, in ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?
        get() = mPresenter

    override fun componentWillMount() {
        super.componentWillMount()
        mPresenter = ContentEntryList2Presenter(this,
            UMFileUtil.parseURLQueryString(queryParams), this,di,this)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        viewMergerHelper.addView(styledDiv { +"Header" })
        viewMergerHelper.addView(styledDiv { +"Footer" })
    }

    override val displayTypeRepo: Any?
        get() = TODO("Not yet implemented")

    override fun showContentEntryAddOptions(parentEntryUid: Long) {
        TODO("Not yet implemented")
    }

    override fun showDownloadDialog(args: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun showMoveEntriesFolderPicker(selectedContentEntryParentChildJoinUids: String) {
        TODO("Not yet implemented")
    }

    override var title: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    override var editOptionVisible: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override var sortOptions: List<MessageIdOption>?
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun componentWillUnmount() {
        super.componentWillUnmount()
        mPresenter.onDestroy()
    }

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