package com.ustadmobile.view

import androidx.paging.DataSource
import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ustadmobile.core.controller.ContentEntryDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ContentEntryDetailOverviewView
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.chipSetFilter
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.contentEntryDetailOverviewComponentOpenBtn
import com.ustadmobile.util.StyleManager.contentEntryDetailOverviewExtraInfo
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.ext.joinString
import com.ustadmobile.view.ext.umEntityAvatar
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.*
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv

class ContentEntryDetailOverviewComponent(mProps: RProps): UstadDetailComponent<ContentEntryWithMostRecentContainer>(mProps),
    ContentEntryDetailOverviewView {

    private lateinit var mPresenter: ContentEntryDetailOverviewPresenter

    private var translations: List<ContentEntryRelatedEntryJoinWithLanguage>? = null

    override val viewName: String
        get() = ContentEntryDetailOverviewView.VIEW_NAME

    override val detailPresenter: UstadDetailPresenter<*, *>
        get() = mPresenter

    override var availableTranslationsList: DataSource.Factory<Int, ContentEntryRelatedEntryJoinWithLanguage>? = null
        get() = field
        set(value) {
            field = value
            GlobalScope.launch(Dispatchers.Main) {
                val relatedTrans = value?.getData(0,100)
                setState {
                    translations = relatedTrans
                }
            }
        }

    override var downloadJobItem: DownloadJobItem? = null
        get() = field
        set(value) {
            //handle download job
            field = value
        }
    override var scoreProgress: ContentEntryStatementScoreProgress?
        get() = TODO("Not yet implemented")
        set(value) {}

    override var locallyAvailable: Boolean = false
        get() = field
        set(value) {
            //handle locally available on web
            field = value
        }
    override var markCompleteVisible: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override var entity: ContentEntryWithMostRecentContainer? = null
        get() = field
        set(value) {
            field = value
        }

    override fun onCreate() {
        super.onCreate()
        mPresenter = ContentEntryDetailOverviewPresenter(this,arguments, this,di,this)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            css{
                +defaultMarginTop
                +contentContainer
            }
            umGridContainer(MGridSpacing.spacing6) {
                umItem(MGridSize.cells12, MGridSize.cells4){
                    css{
                        flexDirection = FlexDirection.column
                    }
                    val fallBackSrc = "assets/"+if(entity?.leaf == true) "book.png" else "folder.png"
                    umEntityAvatar(entity?.thumbnailUrl,fallBackSrc,showIcon = false)

                    mButton(getString(MessageID.open), size = MButtonSize.large
                        ,color = MColor.secondary,variant = MButtonVariant.contained,
                        onClick = {
                            mPresenter.handleOnClickOpenDownloadButton()
                        }){
                        css (contentEntryDetailOverviewComponentOpenBtn)
                    }
                }

                umItem(MGridSize.cells12, MGridSize.cells8){
                    styledDiv {
                        css {
                            +contentEntryDetailOverviewExtraInfo
                        }

                        mTypography(entity?.title, variant = MTypographyVariant.h4,
                            gutterBottom = true){
                            css(alignTextToStart)
                        }

                        mTypography(
                            entity?.author?.let {
                                getString(MessageID.entry_details_author).joinString(it)
                            },
                            variant = MTypographyVariant.h6, gutterBottom = true){
                            css(alignTextToStart)
                        }

                        styledDiv {
                            mGridContainer(spacing= MGridSpacing.spacing10){

                                mGridItem {
                                    mTypography(
                                        entity?.publisher?.let {
                                            getString(MessageID.entry_details_publisher).joinString(":",it)
                                        },
                                        variant = MTypographyVariant.subtitle1, gutterBottom = true){
                                        css(alignTextToStart)
                                    }
                                }

                                mGridItem {
                                    mTypography(
                                        entity?.licenseName?.let {
                                            getString(MessageID.entry_details_license).joinString(it)
                                        },
                                        variant = MTypographyVariant.subtitle1, gutterBottom = true){
                                        css(alignTextToStart)
                                    }
                                }
                            }
                        }

                        mTypography(getString(MessageID.description),
                            variant = MTypographyVariant.caption, paragraph = true){
                            css(alignTextToStart)
                        }

                        mTypography(entity?.description, paragraph = true){
                            css(alignTextToStart)
                        }

                        mTypography(getString(MessageID.also_available_in),
                            variant = MTypographyVariant.caption, paragraph = true){
                            css(alignTextToStart)
                        }
                        styledDiv {
                            css{
                                +chipSetFilter
                                display = displayProperty(translations != null, true)
                            }
                            translations?.forEach { translation ->
                                mChip("${translation.language?.name}",
                                    onClick = {
                                        mPresenter.handleOnTranslationClicked(translation.language?.langUid ?: 0)
                                    }) {
                                    css {
                                        margin(1.spacingUnits)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onFabClicked() {
        super.onFabClicked()
        mPresenter.handleClickEdit()
    }

    override fun showDownloadDialog(args: Map<String, String>) {
        TODO("Not yet implemented")
    }
}