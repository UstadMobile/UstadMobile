package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSiteTermsEditBinding
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.controller.SiteTermsEditPresenter
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SiteTermsEditView
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.port.android.util.ext.*
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.plugins.CssUnderlinePlugin
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener


interface SiteTermsEditFragmentEventHandler {
    fun onClickLanguage()
}

class SiteTermsEditFragment: UstadEditFragment<SiteTermsWithLanguage>(), SiteTermsEditView, SiteTermsEditFragmentEventHandler,
    IAztecToolbarClickListener{

    private var mBinding: FragmentSiteTermsEditBinding? = null

    private var mPresenter: SiteTermsEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, SiteTermsWithLanguage>?
        get() = mPresenter


    private var aztec: Aztec? = null

    override var languageError: String?
        get() = mBinding?.languageError
        set(value) {
            mBinding?.languageError = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSiteTermsEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this
            aztec = Aztec.with(it.editor,  it.formattingToolbar, this).also {
                it.visualEditor.setCalypsoMode(false)
                it.addPlugin(CssUnderlinePlugin())
                it.initSourceEditorHistory()
            }

        }

        mPresenter = SiteTermsEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(backStackSavedState)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
            Language::class.java) {
            val selectedLang = it.firstOrNull() ?: return@observeResult
            entity?.stLanguage = selectedLang
            entity?.sTermsLang = selectedLang.iso_639_1_standard
            entity?.sTermsLangUid = selectedLang.langUid
            mBinding?.siteTerms = entity
        }
    }

    override fun onSaveStateToBackStackStateHandle() {
        mBinding?.siteTerms?.termsHtml = aztec?.visualEditor?.toHtml()
        super.onSaveStateToBackStackStateHandle()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_done) {
            mBinding?.siteTerms?.termsHtml = aztec?.visualEditor?.toHtml()
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

    override var entity: SiteTermsWithLanguage? = null
        get() = field
        set(value) {
            field = value
            mBinding?.siteTerms = value
            val termsHtmlVal = value?.termsHtml
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