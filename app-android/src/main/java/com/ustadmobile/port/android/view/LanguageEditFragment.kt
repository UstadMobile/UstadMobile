package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentLanguageEditBinding
import com.ustadmobile.core.controller.LanguageEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.LanguageEditView
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap


interface LanguageEditFragmentEventHandler {

}

class LanguageEditFragment: UstadEditFragment<Language>(), LanguageEditView, LanguageEditFragmentEventHandler {

    private var mBinding: FragmentLanguageEditBinding? = null

    private var mPresenter: LanguageEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Language>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentLanguageEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = LanguageEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.add_new_language, R.string.edit_language)


        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: Language? = null
        get() = field
        set(value) {
            field = value
            mBinding?.language = value
        }

    override var langNameError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.langNameError = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }
}