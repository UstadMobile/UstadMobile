package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSettingsBinding
import com.ustadmobile.core.controller.SettingsPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SettingsView
import android.widget.AdapterView

class SettingsFragment : UstadBaseFragment(), SettingsView, AdapterView.OnItemClickListener {

    var mPresenter: SettingsPresenter? = null

    private var mBinding: FragmentSettingsBinding? = null

    private var languageOptions: AutoCompleteTextView? = null

    override var workspaceSettingsVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.workspaceSettingsVisible = value
        }
    override var holidayCalendarVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.holidayCalendarVisible = value
        }

    override var reasonLeavingVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.reasonLeavingVisible = value
        }

    override var langListVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.langListVisible = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view:View
        mBinding = FragmentSettingsBinding.inflate(inflater, container, false).also {
            view = it.root
        }

        languageOptions = mBinding?.fragmentSettingsLanguageOptionsAutocompleteTextview

        mPresenter = SettingsPresenter(requireContext(), arguments.toStringMap(),
                this, di).withViewLifecycle()
        mPresenter?.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState))

        mBinding?.presenter = mPresenter

        return view
    }

    override fun setLanguageOptions(languages: List<String>, currentSelection: String) {
        val adapter = ArrayAdapter(requireContext(), R.layout.autocomplete_list_item, languages)
        languageOptions?.setAdapter(adapter)
        languageOptions?.setText(currentSelection, false)
        languageOptions?.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        mPresenter?.handleLanguageSelected(position)
    }

    override fun restartUI() {
        activity?.recreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
    }

}
