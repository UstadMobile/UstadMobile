package com.ustadmobile.view

import androidx.paging.DataSource
import com.ustadmobile.core.controller.ContentEntry2DetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.lib.db.entities.ContentEntryProgress
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.util.RouteManager
import com.ustadmobile.util.RouteManager.getArgs
import react.RBuilder
import react.RProps
import styled.styledDiv

class ContentEntryDetailComponent(mProps: RProps): UstadDetailComponent<ContentEntryWithMostRecentContainer>(mProps),
    ContentEntry2DetailView {

    private lateinit var mPresenter: ContentEntry2DetailPresenter

    override fun componentWillMount() {
        mPresenter = ContentEntry2DetailPresenter(this,getArgs(),
            this,di,this)
        //mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            +"Details"
        }
    }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override var availableTranslationsList: DataSource.Factory<Int, ContentEntryRelatedEntryJoinWithLanguage>?
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun showDownloadDialog(args: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override var downloadJobItem: DownloadJobItem?
        get() = TODO("Not yet implemented")
        set(value) {}

    override var contentEntryProgress: ContentEntryProgress?
        get() = TODO("Not yet implemented")
        set(value) {}

    override var locallyAvailable: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override var entity: ContentEntryWithMostRecentContainer? = null
        get() = field
        set(value) {
            field = value
        }

    override fun onClickSort(sortOption: SortOrderOption) {
        TODO("Not yet implemented")
    }
}