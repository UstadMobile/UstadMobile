package com.ustadmobile.view

import com.ustadmobile.core.controller.LanguageEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.LanguageEditView
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.mui.components.umTextField
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import mui.material.FormControlVariant
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class LanguageEditComponent (mProps: UmProps): UstadEditComponent<Language>(mProps),
    LanguageEditView {

    private var mPresenter: LanguageEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Language>?
        get() = mPresenter

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
        styledDiv {
            css {
                +StyleManager.fieldsOnlyFormScreen
            }

            umItem(GridSize.cells12){
                umTextField(label = "${nameLabel.text}",
                    helperText = nameLabel.errorText,
                    value = entity?.name, error = nameLabel.error,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.name = it
                            langNameError = null
                        }
                    })
            }

            umGridContainer(GridSpacing.spacing4) {
                umItem(GridSize.cells12, GridSize.cells6){
                    umTextField(
                        label = "${twoLetterLabel.text}",
                        helperText = twoLetterLabel.errorText,
                        value = entity?.iso_639_2_standard,
                        error = twoLetterLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.iso_639_2_standard = it
                            }
                        })
                }

                umItem(GridSize.cells12, GridSize.cells6){
                    umTextField(label = "${threeLetterLabel.text}",
                        helperText = threeLetterLabel.errorText,
                        value = entity?.iso_639_3_standard, error = threeLetterLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.iso_639_3_standard = it
                            }
                        })
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
        langNameError = null
    }

}