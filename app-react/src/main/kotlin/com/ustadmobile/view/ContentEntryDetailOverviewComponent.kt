package com.ustadmobile.view

import com.ustadmobile.core.controller.ContentEntryDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.calculateScoreWithPenalty
import com.ustadmobile.core.view.ContentEntryDetailOverviewView
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorMediatorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.chipSetFilter
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.contentEntryDetailOverviewComponentOpenBtn
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.detailContentProgress
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.entryDetailRightSection
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util.ASSET_BOOK
import com.ustadmobile.util.Util.ASSET_FOLDER
import com.ustadmobile.util.ext.joinString
import com.ustadmobile.view.components.AttachmentImageLookupAdapter
import com.ustadmobile.view.ext.*
import kotlinx.css.*
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ContentEntryDetailOverviewComponent(mProps: UmProps): UstadDetailComponent<ContentEntryWithMostRecentContainer>(mProps),
    ContentEntryDetailOverviewView {

    private var mPresenter: ContentEntryDetailOverviewPresenter? = null

    private var translations: List<ContentEntryRelatedEntryJoinWithLanguage> = listOf()


    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private val observer = ObserverFnWrapper<List<ContentEntryRelatedEntryJoinWithLanguage>>{
        if(translations.isEmpty()) return@ObserverFnWrapper
        setState {
            translations = it
        }
    }

    override var availableTranslationsList: DoorDataSourceFactory<Int, ContentEntryRelatedEntryJoinWithLanguage>? = null
        get() = field
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(observer)
            liveData?.observe(this, observer)
        }


    override var scoreProgress: ContentEntryStatementScoreProgress? = null
        get() = field
        set(value) {
            field = value
        }

    override var locallyAvailable: Boolean = false
        get() = field
        set(value) {
            //handle locally available on web
            field = value
        }

    override var markCompleteVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var contentEntryButtons: ContentEntryButtonModel? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var activeContentJobItems: List<ContentJobItemProgress>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override var entity: ContentEntryWithMostRecentContainer? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = ContentEntryDetailOverviewPresenter(this,arguments, this,di,this)
        mPresenter?.onCreate(mapOf())
    }


    override fun RBuilder.render() {
        styledDiv {
            css{
                +defaultMarginTop
                +contentContainer
            }
            umGridContainer(columnSpacing = GridSpacing.spacing4) {
                umItem(GridSize.cells12, GridSize.cells4){
                    css{
                        flexDirection = FlexDirection.column
                    }

                    withAttachmentLocalUrlLookup(entity?.contentEntryUid ?: 0,
                        ATTACHMENT_URI_LOOKUP_ADAPTER
                    ) { attachmentSrc ->
                        umEntityAvatar(attachmentSrc,
                            if(entity?.leaf == true) ASSET_BOOK else ASSET_FOLDER,
                            showIcon = false)
                    }


                    if((scoreProgress?.progress ?: 0) > 0){
                        umLinearProgress((scoreProgress?.progress ?: 0).toDouble(),
                            variant = ProgressVariant.determinate){
                            css (detailContentProgress)
                        }
                    }

                    val buttonLabelId = if(contentEntryButtons?.showDownloadButton == true)
                        MessageID.download else MessageID.open

                    umButton(getString(buttonLabelId),
                        size = ButtonSize.large,
                        color = UMColor.secondary,
                        variant = ButtonVariant.contained,
                        onClick = {
                           if(contentEntryButtons?.showDownloadButton == true){
                               mPresenter?.handleClickDownloadButton()
                           }else {
                               mPresenter?.handleClickOpenButton()
                           }
                        }){
                        css (contentEntryDetailOverviewComponentOpenBtn)
                    }
                }

                umItem(GridSize.cells12, GridSize.cells8){
                    css(entryDetailRightSection)

                    umGridContainer {

                        umItem(GridSize.cells12){
                            umTypography(entity?.title,
                                variant = TypographyVariant.h4,
                                gutterBottom = true){
                                css(alignTextToStart)
                            }
                        }

                        umItem(GridSize.cells12){
                            css {
                                display = displayProperty(!entity?.author.isNullOrEmpty())
                            }
                            umTypography(
                                entity?.author?.let { author ->
                                    getString(MessageID.entry_details_author).joinString(author)
                                },
                                variant = TypographyVariant.h6,
                                gutterBottom = true){
                                css(alignTextToStart)
                            }
                        }

                        umItem(GridSize.cells12) {
                            css {
                                display = displayProperty(!entity?.publisher.isNullOrEmpty())
                            }
                            umTypography(
                                entity?.publisher?.let {
                                    getString(MessageID.entry_details_publisher).joinString(":",it)
                                },
                                variant = TypographyVariant.subtitle1,
                                gutterBottom = true){
                                css(alignTextToStart)
                            }
                        }

                        umGridContainer(spacing= GridSpacing.spacing3){
                            umItem(GridSize.cells12) {
                                css {
                                    display = displayProperty(!entity?.licenseName.isNullOrEmpty())
                                }
                                umTypography(
                                    entity?.licenseName?.let { license ->
                                        getString(MessageID.entry_details_license).joinString(license).joinString(" ${entity?.container?.fileSize?.let {
                                           ", ${ UMFileUtil.formatFileSize(it)}"
                                        } ?: ""}")
                                    },
                                    variant = TypographyVariant.subtitle1,
                                    gutterBottom = true){
                                    css(alignTextToStart)
                                }
                            }


                            umItem(GridSize.cells12) {
                                css{
                                    display = displayProperty((scoreProgress?.progress ?: 0) > 0)
                                }

                                umGridContainer {
                                    umItem(GridSize.cells1) {
                                        umAvatar(className = "${StyleManager.name}-contentEntryListContentAvatarClass") {
                                            umIcon("emoji_events", className= "${StyleManager.name}-contentEntryListContentTyeIconClass"){
                                                css{marginTop = 4.px}
                                            }
                                        }
                                    }

                                    umItem(GridSize.cells1){
                                        umTypography(
                                            "${scoreProgress?.calculateScoreWithPenalty()}%",
                                            variant = TypographyVariant.subtitle1,
                                            gutterBottom = true){
                                            css(alignTextToStart)
                                        }
                                    }

                                    umItem(GridSize.cells2){

                                        umTypography(
                                            "( ${scoreProgress?.resultScore} / ${scoreProgress?.resultMax} )",
                                            variant = TypographyVariant.subtitle1,
                                            gutterBottom = true){
                                            css(alignTextToStart)
                                        }
                                    }
                                }
                            }

                        }

                        umItem(GridSize.cells12){
                            css{
                                paddingTop = 2.spacingUnits
                                display = displayProperty(!entity?.description.isNullOrEmpty())
                            }

                            umTypography(getString(MessageID.description),
                                variant = TypographyVariant.caption,
                                paragraph = true){
                                css(alignTextToStart)
                            }

                            umTypography(entity?.description, paragraph = true){
                                css(alignTextToStart)
                            }
                        }

                        umGridContainer(GridSpacing.spacing4) {
                            css(defaultPaddingTop)
                            renderTopMainAction("library_add_check",getString(MessageID.mark_complete), GridSize.cells4, GridSize.cells4,
                                markCompleteVisible, TypographyVariant.body2){
                                mPresenter?.handleOnClickMarkComplete()
                            }

                            renderTopMainAction("delete",getString(MessageID.delete), GridSize.cells4, GridSize.cells4,
                                markCompleteVisible, TypographyVariant.body2){
                                mPresenter?.handleOnClickConfirmDelete()
                            }

                            renderTopMainAction("download",getString(MessageID.manage_download), GridSize.cells4, GridSize.cells4,
                                markCompleteVisible, TypographyVariant.body2){
                                mPresenter?.handleOnClickManageDownload()
                            }
                        }

                        umItem(GridSize.cells12) {
                            css{
                                display = displayProperty(translations.isNotEmpty())
                            }
                            umTypography(getString(MessageID.also_available_in),
                                variant = TypographyVariant.caption,
                                paragraph = true){
                                css(alignTextToStart)
                            }

                            styledDiv {
                                css(chipSetFilter)
                                translations.forEach { translation ->
                                    umChip("${translation.language?.name}",
                                        onClick = {
                                            mPresenter?.handleOnTranslationClicked(translation.language?.langUid ?: 0)
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
    }

    override fun onFabClicked() {
        super.onFabClicked()
        mPresenter?.handleClickEdit()
    }

    override fun showDownloadDialog(args: Map<String, String>) {
        TODO("showDownloadDialog: Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }

    companion object {

        val ATTACHMENT_URI_LOOKUP_ADAPTER = AttachmentImageLookupAdapter { db, entityUid ->
            object: DoorMediatorLiveData<String?>(), DoorObserver<ContentEntryPicture?> {
                init {
                    addSource(db.contentEntryPictureDao.findByContentEntryUidLive(entityUid), this)
                }

                override fun onChanged(t: ContentEntryPicture?) {
                    postValue(t?.cepUri)
                }
            }
        }
    }

}