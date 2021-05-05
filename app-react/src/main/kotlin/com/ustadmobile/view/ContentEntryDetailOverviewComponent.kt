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
import com.ustadmobile.lib.db.entities.ContentEntryProgress
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.util.CssStyleManager.chipSet
import com.ustadmobile.util.CssStyleManager.defaultMarginTop
import com.ustadmobile.util.CssStyleManager.entryDetailComponentContainer
import com.ustadmobile.util.CssStyleManager.entryDetailComponentEntryExtraInfo
import com.ustadmobile.util.CssStyleManager.entryDetailComponentEntryImage
import com.ustadmobile.util.CssStyleManager.entryDetailComponentEntryImageAndButtonContainer
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.util.ext.joinString
import com.ustadmobile.util.ext.renderEntryThumbnailImg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.Display
import kotlinx.css.display
import kotlinx.css.margin
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv
import styled.styledImg

class ContentEntryDetailOverviewComponent(mProps: RProps): UstadDetailComponent<ContentEntryWithMostRecentContainer>(mProps),
    ContentEntryDetailOverviewView {

    private lateinit var mPresenter: ContentEntryDetailOverviewPresenter

    private var translations: List<ContentEntryRelatedEntryJoinWithLanguage>? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override var availableTranslationsList: DataSource.Factory<Int, ContentEntryRelatedEntryJoinWithLanguage>? = null
        get() = field
        set(value) {
            field = value
            GlobalScope.launch(Dispatchers.Main) {
                val relatedTrans = value?.getData(0,100)
                setState {translations = relatedTrans}
            }
        }

    override var downloadJobItem: DownloadJobItem? = null
        get() = field
        set(value) {
            //handle download job
            field = value
        }

    override var contentEntryProgress: ContentEntryProgress? = null
        get() = field
        set(value) {
            //handle progress
            field = value
        }

    override var locallyAvailable: Boolean = false
        get() = field
        set(value) {
            //handle locally available on web
            field = value
        }

    override var entity: ContentEntryWithMostRecentContainer? = null
        get() = field
        set(value) {
            field = value
        }

    override fun componentDidMount() {
        super.componentDidMount()
        mPresenter = ContentEntryDetailOverviewPresenter(this,getArgs(),
            this,di,this)
        mPresenter.onCreate(mapOf())
    }


    override fun RBuilder.render() {
        styledDiv{
            css{
                +entryDetailComponentContainer
                display = if(entity == null ) Display.none else Display.flex
            }

            styledDiv {
                css{+entryDetailComponentEntryImageAndButtonContainer}
                styledImg {
                    css{+entryDetailComponentEntryImage}
                    attrs{ entity?.let { renderEntryThumbnailImg(it) } }
                }

                mButton(systemImpl.getString(MessageID.open, this), size = MButtonSize.large
                    ,color = MColor.secondary,variant = MButtonVariant.contained, onClick = {
                        mPresenter.handleOnClickOpenDownloadButton() }){
                    css { +defaultMarginTop}
                }
            }

            styledDiv {
                css { +entryDetailComponentEntryExtraInfo }

                mTypography(entity?.title, variant = MTypographyVariant.h4, gutterBottom = true){}

                mTypography(
                    entity?.author?.let {
                        systemImpl.getString(MessageID.entry_details_author, this)
                            .joinString(it)
                    },
                    variant = MTypographyVariant.h6, gutterBottom = true)

                styledDiv {
                    mGridContainer(spacing= MGridSpacing.spacing10){

                        mGridItem {
                            mTypography(
                                entity?.publisher?.let {
                                    systemImpl.getString(MessageID.entry_details_publisher,this)
                                        .joinString(":",it) },
                                variant = MTypographyVariant.subtitle1, gutterBottom = true)
                        }

                        mGridItem {
                            mTypography(
                                entity?.licenseName?.let {
                                    systemImpl.getString(MessageID.entry_details_license,this)
                                        .joinString(it)
                                },
                                variant = MTypographyVariant.subtitle1, gutterBottom = true)
                        }
                    }
                }

                mTypography(systemImpl.getString(MessageID.description,this),
                    variant = MTypographyVariant.caption, paragraph = true)

                mTypography(entity?.description, paragraph = true)

                mTypography(systemImpl.getString(MessageID.also_available_in, this),
                    variant = MTypographyVariant.caption, paragraph = true
                )
                styledDiv {
                    css{
                        +chipSet
                        display = if(translations == null) Display.none else Display.flex
                    }
                    translations?.forEach { translation ->
                        mChip("${translation.language?.name}", onClick = {
                            mPresenter.handleOnTranslationClicked(translation.language?.langUid?:0) }) {
                            css { margin(1.spacingUnits) }
                        }
                    }
                }
            }
        }

    }

    override fun showDownloadDialog(args: Map<String, String>) {
        TODO("Not yet implemented")
    }
}