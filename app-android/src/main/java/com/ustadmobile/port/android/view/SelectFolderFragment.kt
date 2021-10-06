package com.ustadmobile.port.android.view

import SelectFolderView
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.fragment.findNavController
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.ContentEntryEdit2View.Companion.ARG_URI
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.port.android.view.ext.saveResultToBackStackSavedStateHandle
import org.kodein.di.instance
import org.w3c.dom.DocumentFragment

class SelectFolderFragment(private val registry: ActivityResultRegistry? = null) : UstadBaseFragment() {

    var activityResultLauncher: ActivityResultLauncher<Uri>? = null

    val systemImpl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree(),
                registry ?: requireActivity().activityResultRegistry) {

            when {
                it == null -> {
                    findNavController().popBackStack()
                }
                arguments?.containsKey(UstadView.ARG_RESULT_DEST_KEY) == true -> {
                    saveResultToBackStackSavedStateHandle(listOf(it.toString()))
                }
                else -> {
                    val map = arguments.toStringMap()
                    val args = mutableMapOf<String, String>()
                    args[ARG_URI] = it.toString()
                    args[UstadView.ARG_POPUPTO_ON_FINISH] = SelectFolderView.VIEW_NAME
                    args.putFromOtherMapIfPresent(map, UstadView.ARG_LEAF)
                    args.putFromOtherMapIfPresent(map, UstadView.ARG_PARENT_ENTRY_UID)
                    systemImpl.go(ContentEntryEdit2View.VIEW_NAME,
                            args, requireContext())
                }
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activityResultLauncher?.launch(null)
        return null
    }

}