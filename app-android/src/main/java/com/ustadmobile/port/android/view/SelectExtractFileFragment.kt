package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.ustadmobile.core.controller.SelectExtractFilePresenter
import com.ustadmobile.core.controller.SelectExtractFilePresenterCommon
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SelectExtractFileView
import com.ustadmobile.port.android.util.ext.getFileName
import kotlinx.coroutines.launch

class SelectExtractFileFragment(private val registry: ActivityResultRegistry? = null) : UstadBaseFragment(), SelectExtractFileView {

    private var activityResultLauncher: ActivityResultLauncher<Array<String>>? = null

    private var mPresenter: SelectExtractFilePresenterCommon? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument(),
            registry ?: requireActivity().activityResultRegistry) { uri ->

            viewLifecycleOwner.lifecycleScope.launch {
                val fileName = uri?.let { requireContext().contentResolver.getFileName(it) }
                mPresenter?.handleUriSelected(uri?.toString(), fileName)
            }
        }

        mPresenter = SelectExtractFilePresenter(requireContext(),
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
            openFileBrowser()
        }

    private fun openFileBrowser(){
        activityResultLauncher?.launch(acceptedMimeTypes.toTypedArray())
    }

    override var noFileSelectedError: String?= null
    override var unSupportedFileError: String? = null
    override var fieldsEnabled: Boolean = false
    override var entity: Any? = null

}