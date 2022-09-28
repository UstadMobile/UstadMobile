package com.ustadmobile.view

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryPicture
import com.ustadmobile.lib.db.entities.ContentEntryWithBlockAndLanguage
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultDoubleMarginTop
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.switchMargin
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.ext.clean
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.components.AttachmentImageComponent
import com.ustadmobile.view.ext.*
import kotlinx.css.*
import mui.material.ButtonColor
import mui.material.ButtonVariant
import mui.material.FormControlVariant
import mui.material.Size
import mui.material.styles.TypographyVariant
import org.w3c.dom.events.MouseEvent
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ContentEntryEditComponent (mProps: UmProps): UstadEditComponent<ContentEntryWithBlockAndLanguage>(mProps),
    ContentEntryEdit2View {

    private var mPresenter: ContentEntryEdit2Presenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ContentEntryWithBlockAndLanguage>?
        get() = mPresenter

    private var titleLabel = FieldLabel(text = getString(MessageID.title))

    private var descLabel = FieldLabel(text = getString(MessageID.description))

    private var minScoreLabel = FieldLabel(text = getString(MessageID.minimum_score))

    private var authorLabel = FieldLabel(text = getString(MessageID.entry_details_author))

    private var publisherLabel = FieldLabel(text = getString(MessageID.entry_details_publisher))

    private var languageLabel = FieldLabel(text = getString(MessageID.language))

    private val doNotShowBeforeLabel = FieldLabel(text = getString(MessageID.dont_show_before).clean())

    private var startTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var deadlineDateLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.deadline))

    private var deadlineTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var gracePeriodDateLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.end_of_grace_period))

    private var gracePeriodTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var completionCriteriaLabel = FieldLabel(text = getString(MessageID.completion_criteria))

    private var penaltyLabel = FieldLabel(text = getString(MessageID.late_submission_penalty))

    private var licenseLabel = FieldLabel(text = getString(MessageID.licence))

    private var maxPointsLabel = FieldLabel(text = getString(MessageID.maximum_points))

    override var contentEntryPicture: ContentEntryPicture? = null
        get() = field
        set(value) {
            setState{
                field = value
            }
        }

    override var licenceOptions: List<ContentEntryEdit2Presenter.LicenceMessageIdOptions>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var completionCriteriaOptions: List<ContentEntryEdit2Presenter.CompletionCriteriaMessageIdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var selectedStorageIndex: Int = 0
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var titleErrorEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
                if(value){
                    titleLabel = titleLabel.copy(errorText = getString(MessageID.field_required_prompt))
                }
            }
        }

    override var fileImportErrorVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var storageOptions: List<ContainerStorageDir>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var metadataResult: MetadataResult? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var compressionEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override val videoDimensions: Pair<Int, Int> = Pair(0,0)
        get() = field

    var showVideoPreview = false

    private var showWebPreview = false

    override var videoUri: String? = null
        get() = field
        set(value) {
            if(value == null) return
            setState {
                field = value
                showVideoPreview = !value.startsWith("http")
                showWebPreview = value.startsWith("http")
            }
        }
    override var showUpdateContentButton: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var caGracePeriodError: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var caDeadlineError: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var caStartDateError: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var caMaxPointsError: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var startDate: Long = 0
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var startTime: Long = 0
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var deadlineDate: Long = 0
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var deadlineTime: Long = 0
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var gracePeriodDate: Long = 0
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var gracePeriodTime: Long = 0
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    private var gracePeriodVisiblity: Boolean = false
        set(value) {
            setState {
                field = value
            }
        }


    override var timeZone: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    var minScoreVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: ContentEntryWithBlockAndLanguage? = null
        get() = field
        set(value) {
            setState {
                field = value
                if(value?.block?.cbDeadlineDate != Long.MAX_VALUE){
                    gracePeriodVisiblity = true
                }
                minScoreVisible = value?.block?.cbCompletionCriteria == ContentEntry.COMPLETION_CRITERIA_MIN_SCORE
            }
        }

    private var updateItemOptionsDialogVisible: Boolean = false
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.content)
        mPresenter = ContentEntryEdit2Presenter(this, arguments, this,
            this, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        styledDiv {
            css{
                if(entity?.leaf == true){
                    +contentContainer
                }else{
                    +StyleManager.fieldsOnlyFormScreen
                }
                +defaultPaddingTop
            }

            if(updateItemOptionsDialogVisible) {
                renderAddContentEntryOptionsDialog(
                    systemImpl,
                    showCreateNewFolder = false,
                    onClickAddFile = { mPresenter?.onClickImportFile() },
                    onClickAddFromLink = { mPresenter?.onClickImportLink() }
                ) {
                    setState {
                        updateItemOptionsDialogVisible = false
                    }
                }
            }

            umGridContainer(GridSpacing.spacing4) {

                umItem(GridSize.cells12, GridSize.cells4){
                    child(AttachmentImageComponent::class) {
                        attrs.attachmentUri = contentEntryPicture?.cepUri
                        attrs.onNewImageSelected = {
                            setState {
                                console.log("ClazzEditComponent: setting course picture uri = $it")
                                contentEntryPicture?.cepUri = it
                            }
                        }
                        attrs.contentBlock = { attachmentImgSrc ->
                            umEntityAvatar(src = attachmentImgSrc, fallbackSrc = Util.ASSET_ENTRY,
                                listItem = true)
                        }
                    }
                }

                val showPreviews = entity?.leaf == true && videoUri != null
                        && (showWebPreview || showVideoPreview)

                if(showUpdateContentButton) {
                    umItem(GridSize.cells12, GridSize.cells4,
                        alignItems = GridAlignItems.center
                    ){
                        umButton(getString(MessageID.update_content),
                            size = Size.large,
                            color = ButtonColor.secondary,
                            variant = ButtonVariant.contained,
                            onClick = {
                                setState{
                                    updateItemOptionsDialogVisible = true
                                }
                            }){
                            css{
                                padding = "15px"
                                marginTop = LinearDimension("13px")
                                +StyleManager.defaultFullWidth
                            }
                        }
                    }
                }



                if(showPreviews){
                    umItem(GridSize.cells12, GridSize.cells4){
                        css{
                            marginTop = 12.px
                        }

                        if(videoUri != null && (showWebPreview || showVideoPreview)){
                            videoUri?.let {
                                renderIframe(listOf(it), 1)
                            }
                        }

                        if(showUpdateContentButton){
                            if(entity?.leaf == true){
                                umItem(GridSize.cells12){
                                    css{
                                        display = displayProperty(entity?.leaf == true)
                                        +defaultMarginTop
                                    }
                                    umTypography(getString(MessageID.supported_files),
                                        variant = TypographyVariant.body2){
                                        css (StyleManager.alignTextToStart)
                                    }
                                }
                            }
                        }

                    }

                }

                umItem(GridSize.cells12, if(showPreviews) GridSize.cells8 else GridSize.cells12){

                    umTextField(label = "${titleLabel.text}",
                        helperText = titleLabel.errorText,
                        value = entity?.title, error = titleLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.title = it
                            }
                        }){
                        css(StyleManager.defaultFullWidth)
                    }


                    umTextField(label = "${descLabel.text}",
                        value = entity?.description,
                        error = descLabel.error, disabled = !fieldsEnabled,
                        helperText = descLabel.errorText,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.description = it
                            }
                        }){
                        css(StyleManager.defaultFullWidth)
                    }

                    if(entity?.block != null){
                        renderCourseBlockCommonFields(entity?.block,
                            doNotShowBeforeLabel, startDate, startTimeLabel,
                            dateSet = {
                                setState{
                                    startDate = it.getTime().toLong()
                                    caStartDateError = null
                                }
                            }, timeZone, completionCriteriaLabel, completionCriteriaOptions,
                            completionCriteriaSet = {
                                setState {
                                    entity?.block?.cbCompletionCriteria = it
                                    completionCriteriaLabel.errorText = null
                                    minScoreVisible = it == ContentEntry.COMPLETION_CRITERIA_MIN_SCORE
                                }
                            }, maxPointsLabel, maxPointsSet = {
                                setState {
                                    entity?.block?.cbMaxPoints = it
                                    caMaxPointsError = null
                                }
                            }, deadlineDateLabel, deadlineTimeLabel, deadlineDate,
                            deadlineDateSet = {
                                setState{
                                    deadlineDate = it.getTime().toLong()
                                    caDeadlineError = null
                                    gracePeriodVisiblity = true
                                }
                            }, gracePeriodDateLabel, gracePeriodTimeLabel, gracePeriodDate, gracePeriodVisiblity,
                            gracePeriodSet = {
                                gracePeriodDate = it.getTime().toLong()
                                caGracePeriodError = null
                            }, penaltyLabel, penaltySet = {
                                setState{
                                    entity?.block?.cbLateSubmissionPenalty = it
                                }
                            }, getString(MessageID.penalty_label),
                            minScoreVisible, minScoreLabel,
                            minPointsSet = {
                                entity?.block?.cbMinPoints = it
                            })
                    }


                    umTextField(label = "${authorLabel.text}",
                        value = entity?.author,
                        error = authorLabel.error, disabled = !fieldsEnabled,
                        helperText = authorLabel.errorText,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.author = it
                            }
                        }){
                        css(StyleManager.defaultFullWidth)
                    }

                    umTextField(label = "${publisherLabel.text}",
                        value = entity?.publisher,
                        error = publisherLabel.error, disabled = !fieldsEnabled,
                        helperText = publisherLabel.errorText,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.publisher = it
                            }
                        }){
                        css(StyleManager.defaultFullWidth)
                    }

                    umGridContainer(GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextFieldSelect(
                                label = "${licenseLabel.text}",
                                value = entity?.licenseType.toString(),
                                error = licenseLabel.error,
                                disabled = !fieldsEnabled,
                                helperText = licenseLabel.errorText,
                                values = licenceOptions?.map {
                                    Pair(it.code.toString(), it.toString())
                                }?.toList(),
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.licenseType = it.toInt()
                                    }
                                })

                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextField(label = "${languageLabel.text}",
                                helperText = languageLabel.errorText,
                                value = entity?.language?.name,
                                error = languageLabel.error,
                                disabled = !fieldsEnabled,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {}
                                }) {
                                attrs.asDynamic().onClick = {
                                    mPresenter?.handleClickLanguage()
                                }
                            }
                        }
                    }

                    umGridContainer(GridSpacing.spacing4) {

                       if(metadataResult != null){
                           umItem(GridSize.cells12) {
                               css(defaultDoubleMarginTop)
                               createSwitchItem(getString(MessageID.compress), compressionEnabled){
                                   setState {
                                       compressionEnabled = !compressionEnabled
                                   }
                               }
                           }
                       }

                        umItem(GridSize.cells12) {
                            createSwitchItem(getString(MessageID.publicly_accessible), entity?.publik == true){
                                setState {
                                    entity?.publik = !(entity?.publik ?: false)
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }

}

fun RBuilder.createSwitchItem(label: String, enabled: Boolean, onClick: (MouseEvent)->Unit){
    umGridContainer {
        umItem(GridSize.cells11){
            umTypography(label,
                variant = TypographyVariant.body1,
                gutterBottom = true){
                css{
                    +StyleManager.alignTextToStart
                    marginTop = LinearDimension("3px")
                    marginRight = LinearDimension("10px")
                }
            }
        }

        umItem(GridSize.cells1){
            umSwitch(enabled){
                attrs.onClick = {
                    onClick.invoke(it.nativeEvent)
                }
            }
        }
        css{
            +switchMargin
            marginTop = LinearDimension("16px")
            marginBottom = LinearDimension("16px")
        }
    }
}