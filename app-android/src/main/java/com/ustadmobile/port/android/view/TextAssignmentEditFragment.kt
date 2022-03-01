package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentAssignmentTextEditBinding
import com.ustadmobile.core.controller.TextAssignmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.TextAssignmentEditView
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
            }

        }

        mPresenter = TextAssignmentEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(backStackSavedState)

        return rootView
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
    }

    override var entity: CourseAssignmentSubmission? = null
        get() = field
        set(value) {
            field = value
            mBinding?.submission = value
            val termsHtmlVal = value?.casText
            if(termsHtmlVal != null)
                aztec?.visualEditor?.fromHtml(termsHtmlVal)

        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }
}