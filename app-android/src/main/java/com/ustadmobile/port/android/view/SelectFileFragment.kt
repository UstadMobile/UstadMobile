package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import com.ustadmobile.core.controller.SelectFilePresenterCommon
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SelectFileView

class SelectFileFragment(private val registry: ActivityResultRegistry? = null) : UstadBaseFragment(), SelectFileView {

    private var activityResultLauncher: ActivityResultLauncher<Array<String>>? = null

    private var mPresenter: SelectFilePresenterCommon? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument(),
            registry ?: requireActivity().activityResultRegistry) {

            mPresenter?.handleUriSelected(it?.toString())
        }

        mPresenter = SelectFilePresenterCommon(requireContext(),
            arguments.toStringMap(),
            this, di).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return View(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        activityResultLauncher = null
    }

    override var acceptedMimeTypes: List<String> = listOf()
        set(value){
            field = value
            activityResultLauncher?.launch(value.toTypedArray())
        }

    override var noFileSelectedError: String?= null
    override var unSupportedFileError: String? = null
    override var fieldsEnabled: Boolean = false
    override var entity: Any? = null

    override fun showSaveOrDiscardChangesDialog() {

    }
}