package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentAssignmentTextEditBinding
import com.ustadmobile.core.controller.TextAssignmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.countWords
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.TextAssignmentEditView
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.plugins.CssUnderlinePlugin
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener


class TextAssignmentEditFragment: UstadEditFragment<CourseAssignmentSubmission>(), TextAssignmentEditView,
        IAztecToolbarClickListener{

    private var mBinding: FragmentAssignmentTextEditBinding? = null

    private var mPresenter: TextAssignmentEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseAssignmentSubmission>?
        get() = mPresenter
    
    private var aztec: Aztec? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentAssignmentTextEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            aztec = Aztec.with(it.textEditor,  it.textFormattingToolbar, this).also {
                it.visualEditor.setCalypsoMode(false)
                it.addPlugin(CssUnderlinePlugin())
                it.initSourceEditorHistory()
                it.visualEditor.isEnabled = (arguments?.get(TextAssignmentEditView.EDIT_ENABLED).toString().toBoolean())
            }
        }

        aztec?.visualEditor?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // this is kept in beforeTextChanged to stop user from typing more words
                val editText = aztec?.visualEditor ?: return
                val length = if(limitType == ClazzAssignment.TEXT_WORD_LIMIT) s.toString().countWords() else s.toString().length

                if (length > charWordLimit) {
                    setCharLimit(editText, editText.text.length)
                } else {
                    removeFilter(editText);
                }
                mBinding?.wordLimit?.text = "$length/$charWordLimit $limitTypeText"
            }
            override fun onTextChanged(url: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {

            }
        })

        mPresenter = TextAssignmentEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(backStackSavedState)

        return rootView
    }

    var charWordLimit = 0

    var limitType = ClazzAssignment.TEXT_CHAR_LIMIT

    var limitTypeText = ""

    override var clazzAssignment: ClazzAssignment? = null
        set(value){
            field = value
            charWordLimit = value?.caTextLimit ?: 0
            limitType = value?.caTextLimitType ?: ClazzAssignment.TEXT_CHAR_LIMIT
            limitTypeText = if(limitType == ClazzAssignment.TEXT_CHAR_LIMIT)
                    requireContext().getString(R.string.characters)
                    else requireContext().getString(R.string.words)

            val text = entity?.casText ?: ""
            val wordsLength = if(limitType == ClazzAssignment.TEXT_WORD_LIMIT) text.countWords() else text.length
            // count == 0 means a new word is going to start
            mBinding?.wordLimit?.text = "$wordsLength/$charWordLimit $limitTypeText"

        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onSaveStateToBackStackStateHandle() {
        mBinding?.submission?.casText = aztec?.visualEditor?.toHtml()
        super.onSaveStateToBackStackStateHandle()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_done) {
            mBinding?.submission?.casText = aztec?.visualEditor?.toHtml()
        }

        return super.onOptionsItemSelected(item)
    }

    private var filter: InputFilter? = null

    private fun setCharLimit(et: EditText, max: Int) {
        filter = LengthFilter(max)
        et.filters = arrayOf<InputFilter>(filter as LengthFilter)
    }

    private fun removeFilter(et: EditText) {
        if (filter != null) {
            et.filters = arrayOfNulls(0)
            filter = null
        }
    }

    override fun onToolbarCollapseButtonClicked() {

    }

    override fun onToolbarExpandButtonClicked() {
    }

    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {
    }

    override fun onToolbarHeadingButtonClicked() {
    }

    override fun onToolbarHtmlButtonClicked() {
    }

    override fun onToolbarListButtonClicked() {
    }

    override fun onToolbarMediaButtonClicked(): Boolean {
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        aztec = null
    }

    override var entity: CourseAssignmentSubmission? = null
        get() = field
        set(value) {
            field = value
            mBinding?.submission = value
            val termsHtmlVal = value?.casText
            if(termsHtmlVal != null)
                aztec?.visualEditor?.fromHtml(termsHtmlVal)

            val text = value?.casText ?: ""
            val wordsLength = if(limitType == ClazzAssignment.TEXT_WORD_LIMIT) text.countWords() else text.length
            // count == 0 means a new word is going to start
            mBinding?.wordLimit?.text = "$wordsLength/$charWordLimit $limitTypeText"
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }
}