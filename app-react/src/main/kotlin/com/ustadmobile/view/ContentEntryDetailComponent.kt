package com.ustadmobile.view

import androidx.paging.DataSource
import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ustadmobile.core.controller.ContentEntry2DetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.CssStyleManager.defaultMarginTop
import com.ustadmobile.util.CssStyleManager.entryDetailComponentContainer
import com.ustadmobile.util.CssStyleManager.entryDetailComponentEntryExtraInfo
import com.ustadmobile.util.CssStyleManager.entryDetailComponentEntryImage
import com.ustadmobile.util.CssStyleManager.entryDetailComponentEntryImageAndButtonContainer
import com.ustadmobile.util.CssStyleManager.entryDetailComponentLanguageList
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.util.UmReactUtil.formatString
import kotlinx.browser.window
import kotlinx.css.margin
import react.RBuilder
import react.RProps
import styled.css
import styled.styledDiv
import styled.styledImg

class ContentEntryDetailComponent(mProps: RProps): UstadDetailComponent<ContentEntryWithMostRecentContainer>(mProps),
    ContentEntry2DetailView {

    private lateinit var mPresenter: ContentEntry2DetailPresenter

    override fun componentDidMount() {
        super.componentDidMount()
        mPresenter = ContentEntry2DetailPresenter(this,getArgs(),
            this,di,this)
        //mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        //change this
        val entity = (window.asDynamic().entries as List<ContentEntryWithMostRecentContainer>).first {
            it.contentEntryUid.toString() == getArgs()[UstadView.ARG_ENTITY_UID]
        }

        val languages = window.asDynamic().languages as List<Language>

        styledDiv{
            css{+entryDetailComponentContainer}

            styledDiv {
                css{+entryDetailComponentEntryImageAndButtonContainer}
                styledImg {
                    css{+entryDetailComponentEntryImage}
                    attrs{
                        src = entity.thumbnailUrl.toString()
                    }
                }

                mButton(systemImpl.getString(MessageID.open, this), size = MButtonSize.large
                ,color = MColor.secondary,variant = MButtonVariant.contained, onClick = {
                        mPresenter.handleOnClickOpenDownloadButton() }){
                    css { +defaultMarginTop}
                }
            }

            styledDiv {
                css { +entryDetailComponentEntryExtraInfo }

                mTypography(entity.title, variant = MTypographyVariant.h4, gutterBottom = true)

                mTypography( formatString(MessageID.entry_details_author,entity.author,systemImpl),
                    variant = MTypographyVariant.h6, gutterBottom = true)

                 styledDiv {
                    css{+entryDetailComponentContainer}
                    mGridContainer(spacing= MGridSpacing.spacing10){

                        mGridItem {
                            mTypography(
                                formatString(MessageID.entry_details_publisher,entity.publisher,systemImpl),
                                variant = MTypographyVariant.subtitle1, gutterBottom = true)
                        }

                        mGridItem {
                            mTypography(
                                formatString(MessageID.entry_details_license,entity.licenseName,systemImpl,""),
                                variant = MTypographyVariant.subtitle1, gutterBottom = true)
                        }
                    }
                }

                mTypography(systemImpl.getString(MessageID.description,this),
                    variant = MTypographyVariant.caption, paragraph = true)

                mTypography(entity.description, paragraph = true)

                mTypography(systemImpl.getString(MessageID.also_available_in, this),
                    variant = MTypographyVariant.caption, paragraph = true
                )
                styledDiv {
                    css{+entryDetailComponentLanguageList}
                    languages.forEach { language ->
                        mChip("${language.name}", onClick = {
                            mPresenter.handleOnTranslationClicked(language.langUid) }) {
                            css { margin(1.spacingUnits) }
                        }
                    }
                }
            }
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