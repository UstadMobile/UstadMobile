package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.*
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.form.mFormControl
import com.ccfraser.muirwik.components.form.mFormHelperText
import com.ccfraser.muirwik.components.input.mInputLabel
import com.ccfraser.muirwik.components.input.mOutlinedInput
import com.ccfraser.muirwik.components.menu.mMenuItem
import com.ustadmobile.FieldLabel
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.*
import kotlinx.html.InputType
import org.w3c.dom.events.MouseEvent
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv

class ContentEntryEditComponent (mProps: RProps): UstadEditComponent<ContentEntryWithLanguage>(mProps),
    ContentEntryEdit2View {

    private var mPresenter: ContentEntryEdit2Presenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ContentEntryWithLanguage>?
        get() = mPresenter

    override val viewName: String
        get() = ContentEntryEdit2View.VIEW_NAME

    private var titleLabel = FieldLabel(text = getString(MessageID.title))

    private var descLabel = FieldLabel(text = getString(MessageID.description))

    private var minScoreLabel = FieldLabel(text = getString(MessageID.minimum_score))

    private var authorLabel = FieldLabel(text = getString(MessageID.entry_details_author))

    private var publisherLabel = FieldLabel(text = getString(MessageID.entry_details_publisher))

    private var languageLabel = FieldLabel(text = getString(MessageID.language))

    private var completionLabel = FieldLabel(text = getString(MessageID.completion_criteria))

    private var licenseLabel = FieldLabel(text = getString(MessageID.licence))

    override var showCompletionCriteria: Boolean = false
        get() = field
        set(value) {
            setState {
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
            }
        }

    override var fileImportErrorVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var storageOptions: List<UMStorageDir>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entryMetaData: ImportedContentEntryMetaData? = null
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

    var showWebPreview = false


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

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: ContentEntryWithLanguage? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        title = getString(MessageID.content)
        mPresenter = ContentEntryEdit2Presenter(this, arguments, this,
            this, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        styledDiv {
            css{
                if(entity?.leaf == true){
                    +StyleManager.contentContainer
                }else{
                    +StyleManager.fieldsOnlyFormScreen
                }
                +StyleManager.defaultPaddingTop
            }

            umGridContainer(MGridSpacing.spacing4) {

                if(entity?.leaf == true){
                    umItem(MGridSize.cells12, MGridSize.cells4){
                        css{
                            marginTop = 12.px
                        }

                        if(videoUri != null && (showWebPreview || showVideoPreview)){
                            videoUri?.let {
                                renderIframe(listOf(it), 1)
                            }
                        }

                        if((entity?.leaf == true || (entity?.contentEntryUid ?: 0L) != 0L)){
                            mButton(getString(MessageID.update_content),
                                size = MButtonSize.large,
                                color = MColor.secondary,
                                variant = MButtonVariant.contained,
                                onClick = {
                                    //update
                                }){
                                css{
                                    padding = "15px"
                                    marginTop = LinearDimension("13px")
                                    +StyleManager.defaultFullWidth
                                }
                            }
                        }


                        if(entity?.leaf == true){
                            umItem(MGridSize.cells12){
                                css{
                                    display = displayProperty(entity?.leaf == true)
                                }
                                mTypography(getString(MessageID.supported_files),
                                    variant = MTypographyVariant.body2,
                                    color = MTypographyColor.textPrimary){
                                    css (StyleManager.alignTextToStart)
                                }
                            }
                        }

                    }

                }

                umItem(MGridSize.cells12, if(entity?.leaf == true) MGridSize.cells8 else MGridSize.cells12){

                    mTextField(label = "${titleLabel.text}",
                        helperText = titleLabel.errorText,
                        value = entity?.title, error = titleLabel.error,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.title = it.targetInputValue
                            }
                        }){
                        css(StyleManager.defaultFullWidth)
                    }


                    mTextField(label = "${descLabel.text}",
                        value = entity?.description,
                        error = descLabel.error, disabled = !fieldsEnabled,
                        helperText = descLabel.errorText,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.description = it.targetInputValue
                            }
                        }){
                        css(StyleManager.defaultFullWidth)
                    }


                    if(showCompletionCriteria){
                        umGridContainer(MGridSpacing.spacing4) {

                            umItem(MGridSize.cells12, MGridSize.cells6 ) {
                                css(StyleManager.defaultMarginTop)
                                mFormControl(variant = MFormControlVariant.outlined) {
                                    css(StyleManager.defaultFullWidth)
                                    mInputLabel("${completionLabel.text}",
                                        htmlFor = "completion",
                                        variant = MFormControlVariant.outlined) {
                                        css(StyleManager.alignTextToStart)
                                    }
                                    mSelect("${entity?.completionCriteria ?: 0}",
                                        native = false,
                                        input = mOutlinedInput(name = "completion",
                                            id = "completion", addAsChild = false,
                                            labelWidth = completionLabel.width),
                                        onChange = { it, _ ->
                                            setState {
                                                entity?.completionCriteria = it.targetValue.toString().toInt()
                                            }
                                        }) {
                                        completionCriteriaOptions?.forEach {
                                            mMenuItem(primaryText = it.toString(), value = it.messageId.toString()){
                                                css(StyleManager.alignTextToStart)
                                            }
                                        }
                                    }
                                    completionLabel.errorText?.let { error ->
                                        mFormHelperText(error){
                                            css(StyleManager.errorTextClass)
                                        }
                                    }
                                }
                            }

                            umItem(MGridSize.cells12, MGridSize.cells6 ) {

                                mTextField(label = "${minScoreLabel.text}",
                                    value = "${entity?.minScore}",
                                    error = minScoreLabel.error, disabled = !fieldsEnabled,
                                    helperText = minScoreLabel.errorText,
                                    variant = MFormControlVariant.outlined,
                                    type = InputType.number,
                                    onChange = {
                                        it.persist()
                                        setState {
                                            entity?.minScore = if(it.targetInputValue.isEmpty()) 0 else it.targetInputValue.toInt()
                                        }
                                    }){
                                    css(StyleManager.defaultFullWidth)
                                }

                            }
                        }
                    }

                    mTextField(label = "${authorLabel.text}",
                        value = entity?.author,
                        error = authorLabel.error, disabled = !fieldsEnabled,
                        helperText = authorLabel.errorText,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.author = it.targetInputValue
                            }
                        }){
                        css(StyleManager.defaultFullWidth)
                    }

                    mTextField(label = "${publisherLabel.text}",
                        value = entity?.publisher,
                        error = publisherLabel.error, disabled = !fieldsEnabled,
                        helperText = publisherLabel.errorText,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.publisher = it.targetInputValue
                            }
                        }){
                        css(StyleManager.defaultFullWidth)
                    }

                    umGridContainer(MGridSpacing.spacing4) {
                        umItem(MGridSize.cells12, MGridSize.cells6 ) {
                            css(StyleManager.defaultMarginTop)
                            mFormControl(variant = MFormControlVariant.outlined) {
                                css(StyleManager.defaultFullWidth)
                                mInputLabel("${licenseLabel.text}",
                                    htmlFor = "licence",
                                    variant = MFormControlVariant.outlined) {
                                    css(StyleManager.alignTextToStart)
                                }
                                mSelect("${entity?.licenseType ?: 0}",
                                    native = false,
                                    input = mOutlinedInput(name = "licence",
                                        id = "licence", addAsChild = false,
                                        labelWidth = completionLabel.width),
                                    onChange = { it, _ ->
                                        setState {
                                            entity?.licenseType = it.targetValue.toString().toInt()
                                        }
                                    }) {
                                    licenceOptions?.forEach {
                                        mMenuItem(primaryText = it.toString(), value = it.messageId.toString()){
                                            css(StyleManager.alignTextToStart)
                                        }
                                    }
                                }
                                licenseLabel.errorText?.let { error ->
                                    mFormHelperText(error){
                                        css(StyleManager.errorTextClass)
                                    }
                                }
                            }
                        }

                        umItem(MGridSize.cells12, MGridSize.cells6 ) {
                            mTextField(label = "${languageLabel.text}",
                                helperText = languageLabel.errorText,
                                value = entity?.language?.name,
                                error = languageLabel.error,
                                disabled = !fieldsEnabled,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
                                    setState {}
                                }) {
                                attrs.asDynamic().onClick = {
                                    mPresenter?.handleClickLanguage()
                                }
                                css(StyleManager.defaultFullWidth)
                            }
                        }
                    }

                    umGridContainer(MGridSpacing.spacing4) {

                       if(entryMetaData != null){
                           umItem(MGridSize.cells12, if(entryMetaData == null) MGridSize.cells6 else MGridSize.cells12) {
                               createSwitchItem(getString(MessageID.compress), compressionEnabled){
                                   setState {
                                       compressionEnabled = !compressionEnabled
                                   }
                               }
                           }
                       }

                        umItem(MGridSize.cells12, if(entryMetaData != null) MGridSize.cells6 else MGridSize.cells12) {
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
        umItem(MGridSize.cells11){
            mTypography(label,
                variant = MTypographyVariant.body1,
                color = MTypographyColor.textPrimary,
                gutterBottom = true){
                css{
                    +StyleManager.alignTextToStart
                    marginTop = LinearDimension("3px")
                }
            }
        }

        umItem(MGridSize.cells1){
            mSwitch(enabled){
                attrs.onClick = onClick
            }
        }
        css{
            marginLeft = LinearDimension("20px")
            marginTop = LinearDimension("16px")
            marginBottom = LinearDimension("16px")
        }
    }
}