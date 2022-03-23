package com.ustadmobile.port.android.view

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import com.ustadmobile.core.controller.SelectFolderPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SelectFolderView

class SelectFolderFragment(private val registry: ActivityResultRegistry? = null) : UstadBaseFragment(),
    SelectFolderView {

    private var activityResultLauncher: ActivityResultLauncher<Uri>? = null

    private var mPresenter: SelectFolderPresenter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        activityResultLauncher = registerForActivityResult<Uri?, Uri?>(ActivityResultContracts.OpenDocumentTree(),
            registry ?: requireActivity().activityResultRegistry) {

            mPresenter?.handleUriSelected(it?.toString())
        }

        mPresenter = SelectFolderPresenter(requireContext(),
            arguments.toStringMap(),
            this, di).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        activityResultLauncher?.launch(null)

        // ustadBaseController does not accept null as view with lifecycle
        return View(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        activityResultLauncher = null
    }


}