package com.ustadmobile.view

import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.LanguageEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.LanguageEditView
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import react.RBuilder
import com.ustadmobile.util.*
import react.setState

class LanguageEditComponent (mProps: UmProps): UstadEditComponent<Language>(mProps),
    LanguageEditView {

    private var mPresenter: LanguageEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Language>?
        get() = mPresenter

    override val viewName: String
        get() = LanguageEditView.VIEW_NAME

    private var nameLabel = FieldLabel(text = getString(MessageID.name))

    private var twoLetterLabel = FieldLabel(text = getString(MessageID.two_letter_code))

    private var threeLetterLabel = FieldLabel(text = getString(MessageID.three_letter_code))

    override var langNameError: String? = null
        get() = field
        set(value) {
            setState {
                nameLabel = nameLabel.copy(errorText = value)
            }
        }


    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: Language? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        mPresenter = LanguageEditPresenter(this, arguments, this,
            this,di)
        setEditTitle(MessageID.add_new_language, MessageID.edit_language)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {

       /* styledDiv {
            css {
                +StyleManager.fieldsOnlyFormScreen
            }

            umItem(MGridSize.cells12){
                mTextField(label = "${nameLabel.text}",
                    helperText = nameLabel.errorText,
                    value = entity?.name, error = nameLabel.error,
                    disabled = !fieldsEnabled,
                    variant = MFormControlVariant.outlined,
                    onChange = {
                        it.persist()
                        setState {
                            entity?.name = it.targetInputValue
                            langNameError = null
                        }
                    }){
                    css(defaultFullWidth)
                }
            }

            umGridContainer(MGridSpacing.spacing4) {
                umItem(MGridSize.cells12, MGridSize.cells6){
                    mTextField(label = "${twoLetterLabel.text}",
                        helperText = twoLetterLabel.errorText,
                        value = entity?.iso_639_2_standard, error = twoLetterLabel.error,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.iso_639_2_standard = it.targetInputValue
                            }
                        }){
                        css(defaultFullWidth)
                    }
                }

                umItem(MGridSize.cells12, MGridSize.cells6){
                    mTextField(label = "${threeLetterLabel.text}",
                        helperText = threeLetterLabel.errorText,
                        value = entity?.iso_639_3_standard, error = threeLetterLabel.error,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.iso_639_3_standard = it.targetInputValue
                            }
                        }){
                        css(defaultFullWidth)
                    }
                }
            }
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
        langNameError = null
    }

}