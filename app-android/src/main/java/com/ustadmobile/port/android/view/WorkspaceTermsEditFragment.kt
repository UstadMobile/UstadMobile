package com.ustadmobile.port.android.view

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentWorkspaceTermsEditBinding
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.controller.WorkspaceTermsEditPresenter
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.WorkspaceTermsEditView
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.WorkspaceTerms
import com.ustadmobile.lib.db.entities.WorkspaceTermsWithLanguage
import com.ustadmobile.port.android.util.ext.*
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.plugins.CssUnderlinePlugin
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener


interface WorkspaceTermsEditFragmentEventHandler {
    fun onClickLanguage()
}

class WorkspaceTermsEditFragment: UstadEditFragment<WorkspaceTermsWithLanguage>(), WorkspaceTermsEditView, WorkspaceTermsEditFragmentEventHandler,
    IAztecToolbarClickListener{

    private var mBinding: FragmentWorkspaceTermsEditBinding? = null

    private var mPresenter: WorkspaceTermsEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, WorkspaceTermsWithLanguage>?
        get() = mPresenter


    private var aztec: Aztec? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentWorkspaceTermsEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this
            aztec = Aztec.with(it.editor,  it.formattingToolbar, this).also {
                it.visualEditor.setCalypsoMode(false)
                it.addPlugin(CssUnderlinePlugin())
                it.initSourceEditorHistory()
            }

        }

        mPresenter = WorkspaceTermsEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)
        mPresenter?.onCreate(backStackSavedState)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
            Language::class.java) {
            val selectedLang = it.firstOrNull() ?: return@observeResult
            entity?.wtLanguage = selectedLang
            entity?.wtLang = selectedLang.iso_639_1_standard
            mBinding?.workspaceTerms = entity
        }
    }

    override fun onSaveStateToBackStackStateHandle() {
        mBinding?.workspaceTerms?.termsHtml = aztec?.visualEditor?.toHtml()
        super.onSaveStateToBackStackStateHandle()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_done) {
            mBinding?.workspaceTerms?.termsHtml = aztec?.visualEditor?.toHtml()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onClickLanguage() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(Language::class.java, R.id.language_list_dest)
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

    override var entity: WorkspaceTermsWithLanguage? = null
        get() = field
        set(value) {
            field = value
            mBinding?.workspaceTerms = value
            val termsHtmlVal = value?.termsHtml
            if(termsHtmlVal != null)
                aztec?.visualEditor?.fromHtml(termsHtmlVal)

        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
}