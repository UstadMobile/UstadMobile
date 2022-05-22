package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContentEntryAddOptionsBinding
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.controller.ContentEntryAddOptionsListener
import com.ustadmobile.core.controller.ContentEntryList2Presenter.Companion.KEY_SELECTED_ITEMS
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList

/**
 * Fragment class responsible for content creation selection, you can create content by one of the following
 * CONTENT_CREATE_FOLDER = Create new content category
 * CONTENT_IMPORT_FILE = Create content from file (epub, h5p e.t.c)
 * CONTENT_CREATE_CONTENT = create content from out content editor
 */

class ContentEntryAddOptionsBottomSheetFragment(
    var listener: ContentEntryAddOptionsListener? = null
) : BottomSheetDialogFragment(), ContentEntryAddOptionsView, View.OnClickListener {


    private var createFolderOptionView: View? = null

    private var addLinkOptionView: View? = null

    private var addFolderOptionView: View? = null

    private var addGalleryOptionView: View? = null

    private var addFileOptionView: View? = null

    private lateinit var argsMap: Map<String, String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        argsMap = arguments.toStringMap()
        val showFolder = argsMap[ARG_SHOW_ADD_FOLDER].toBoolean()
        return FragmentContentEntryAddOptionsBinding.inflate(inflater, container, false).also {
            createFolderOptionView = it.contentCreateFolder
            addLinkOptionView = it.contentAddLink
            addGalleryOptionView = it.contentAddGallery
            addFileOptionView = it.contentAddFile
            addFolderOptionView = it.contentAddFolder
            it.showFolder = showFolder
            createFolderOptionView?.setOnClickListener(this)
            addLinkOptionView?.setOnClickListener(this)
            addFileOptionView?.setOnClickListener(this)
            addGalleryOptionView?.setOnClickListener(this)
            addFolderOptionView?.setOnClickListener(this)
        }.root
    }

    override fun onClick(view: View?) {
        when(view?.id){

            R.id.content_add_link -> {
                listener?.onClickImportLink()
            }
            R.id.content_add_gallery ->{
                listener?.onClickImportGallery()
            }
            R.id.content_add_file ->{
                listener?.onClickImportFile()
            }
            R.id.content_add_folder ->{
                listener?.onClickAddFolder()
            }
            R.id.content_create_folder ->{
                listener?.onClickNewFolder()
            }
        }

        listener = null
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        addLinkOptionView?.setOnClickListener(null)
        createFolderOptionView?.setOnClickListener(null)
        addGalleryOptionView?.setOnClickListener(null)
        addFileOptionView?.setOnClickListener(null)
        addFolderOptionView?.setOnClickListener(null)
        createFolderOptionView = null
        addLinkOptionView = null
        addFolderOptionView = null
        addGalleryOptionView = null
        addFileOptionView = null
        listener = null
    }

    companion object {

        const val ARG_SHOW_ADD_FOLDER = "showFolder"

    }

}
