package com.ustadmobile.view

import com.ustadmobile.core.controller.TextAssignmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.countWords
import com.ustadmobile.core.view.TextAssignmentEditView
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.mui.components.TypographyVariant
import com.ustadmobile.mui.components.spacingUnits
import com.ustadmobile.mui.components.umTypography
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umSpacer
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class TextAssignmentEditComponent (mProps: UmProps): UstadEditComponent<CourseAssignmentSubmission>(mProps),
    TextAssignmentEditView {

    private var mPresenter: TextAssignmentEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseAssignmentSubmission>?
        get() = mPresenter

    var limitType = ClazzAssignment.TEXT_CHAR_LIMIT

    private var limitTypeText = ""

    var limitTextLabel = ""

    var editEnabled = true

    var charWordLimit = 0

    var maxCharOnEditor: Int? = null

    var editorContent = ""

    var editorContentChangedTo: String ? = null
        get() = field
        set(value) {
            field = value
            updateWordsFilter()
        }

    override var clazzAssignment: ClazzAssignment? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
            charWordLimit = value?.caTextLimit ?: 0
            limitType = value?.caTextLimitType ?: ClazzAssignment.TEXT_CHAR_LIMIT
            limitTypeText = if(limitType == ClazzAssignment.TEXT_CHAR_LIMIT)
                getString(MessageID.characters)
            else getString(MessageID.words)
            updateWordsFilter()
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: CourseAssignmentSubmission? = null
        get() = field
        set(value) {
            editorContentChangedTo = value?.casText
            editorContent = editorContentChangedTo ?: ""
            field = value
        }

    private fun updateWordsFilter(){
        setState {
            editEnabled = arguments[TextAssignmentEditView.EDIT_ENABLED].toString().toBoolean()
            val text = editorContentChangedTo ?: ""
            ustadComponentTitle = clazzAssignment?.caTitle
            val wordsLength = if(limitType == ClazzAssignment.TEXT_WORD_LIMIT)
                text.countWords() else text.length
            val maxReached = wordsLength/charWordLimit == 1
            if(maxReached){
                maxCharOnEditor = text.length
            }
            limitTextLabel = "$wordsLength/$charWordLimit $limitTypeText"
        }
    }

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = TextAssignmentEditPresenter(this, arguments, this,
            this,di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        updateUiWithStateChangeDelay {
            fabManager?.visible = arguments[TextAssignmentEditView.EDIT_ENABLED].toString().toBoolean()
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +StyleManager.fieldsOnlyFormScreen
            }

            umItem{
                umMuiHtmlEditor(editorContent,
                    readOnly = !editEnabled,
                    label = getString(MessageID.type_here),
                    maxLength = maxCharOnEditor,
                    onChange = {
                        entity?.casText = it
                       setState {
                           editorContentChangedTo = it
                       }
                    }
                )
            }
            umSpacer(top = 6.spacingUnits)
            umItem {
                umTypography(limitTextLabel, variant = TypographyVariant.body1)
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