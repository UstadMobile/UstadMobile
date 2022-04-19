package com.ustadmobile.view

import com.ustadmobile.core.controller.CourseTerminologyEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.CourseTerminologyEditView
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.TerminologyEntry
import com.ustadmobile.mui.components.FormControlVariant
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.mui.components.umTextField
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.renderListSectionTitle
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umSpacer
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class CourseTerminologyEditComponent (mProps: UmProps): UstadEditComponent<CourseTerminology>(mProps),
    CourseTerminologyEditView {

    private var mPresenter: CourseTerminologyEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseTerminology>?
        get() = mPresenter

    private var titleLabel = FieldLabel(text = getString(MessageID.name))

    override var titleErrorText: String? = null
        get() = field
        set(value) {
            field = value
            setState {
                titleLabel = titleLabel.copy(errorText = value)
            }
        }


    override var terminologyTermList: List<TerminologyEntry>? = null
        get() = field
        set(value) {
            field = value
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

    override var entity: CourseTerminology? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        mPresenter = CourseTerminologyEditPresenter(this, arguments, this, this, di)
        setEditTitle(MessageID.add_new_terminology, MessageID.edit_terminology)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +StyleManager.fieldsOnlyFormScreen
            }

            umItem(GridSize.cells12){
                umTextField(label = "${titleLabel.text}",
                    helperText = titleLabel.errorText,
                    value = entity?.ctTitle,
                    error = titleLabel.error,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.ctTitle = it
                            titleErrorText = null
                        }
                    }
                )
            }

            umSpacer()
            renderListSectionTitle(getString(MessageID.your_words_for))
            umSpacer()

            umGridContainer(GridSpacing.spacing4) {

                terminologyTermList?.forEachIndexed { index, item ->
                    val fieldLabel = FieldLabel(getString(item.messageId),
                        errorText = item.errorMessage)
                    umItem(GridSize.cells6){
                        umTextField(label = "${fieldLabel.text}",
                            helperText = fieldLabel.errorText,
                            value = item.term,
                            error = fieldLabel.error,
                            disabled = !fieldsEnabled,
                            variant = FormControlVariant.outlined,
                            onChange = {
                                setState {
                                    terminologyTermList!![index].term = it
                                    terminologyTermList!![index].errorMessage = null
                                }
                            }
                        )
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
        titleErrorText = null
    }

}