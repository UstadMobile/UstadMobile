package com.ustadmobile.port.android.view

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UMAndroidUtil
import com.toughra.ustadmobile.databinding.FragmentContentEntryAddOptionsBinding
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.controller.ContentEntryList2Presenter
import com.ustadmobile.core.controller.ContentEntryList2Presenter.Companion.KEY_SELECTED_ITEMS
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.util.ext.putResultDestInfo
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_GO_TO_COMPLETE
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_POPUPTO_ON_FINISH
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList

/**
 * Fragment class responsible for content creation selection, you can create content by one of the following
 * CONTENT_CREATE_FOLDER = Create new content category
 * CONTENT_IMPORT_FILE = Create content from file (epub, h5p e.t.c)
 * CONTENT_CREATE_CONTENT = create content from out content editor
 */

class ContentEntryAddOptionsBottomSheetFragment(val showFolderOption: Boolean = true) : BottomSheetDialogFragment(), ContentEntryAddOptionsView, View.OnClickListener {


    private var createFolderOptionView: View? = null

    private var addLinkOptionView: View? = null

    private var addGalleryOptionView: View? = null

    private var addFileOptionView: View? = null

    private lateinit var argsMap: Map<String, String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        argsMap = arguments.toStringMap()
        return FragmentContentEntryAddOptionsBinding.inflate(inflater, container, false).also {
            createFolderOptionView = it.contentCreateFolder
            addLinkOptionView = it.contentAddLink
            addGalleryOptionView = it.contentAddGallery
            addFileOptionView = it.contentAddFile
            it.showFolder = showFolderOption
            createFolderOptionView?.setOnClickListener(this)
            addLinkOptionView?.setOnClickListener(this)
            addFileOptionView?.setOnClickListener(this)
            addGalleryOptionView?.setOnClickListener(this)
        }.root
    }

    override fun onClick(view: View?) {
        val isShowing = this.dialog?.isShowing


        val args = mutableMapOf(ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(argsMap, ARG_PARENT_ENTRY_UID)
        args.putFromOtherMapIfPresent(argsMap, KEY_SELECTED_ITEMS)

        when(view?.id){

            R.id.content_add_link -> {
                if(showFolderOption){
                    findNavController().navigate(R.id.import_link_view, args.toBundle())
                }else{
                   navigateToPickEntityFromList(ImportedContentEntryMetaData::class.java,
                   R.id.import_link_view, args.toBundle())
                }
            }
            R.id.content_add_gallery ->{
                args[SelectFileView.ARG_SELECT_FILE] = SelectFileView.SELECT_GALLERY

                if(showFolderOption){
                    findNavController().navigate(R.id.select_file_view, args.toBundle())
                }else{
                    navigateToPickEntityFromList(String::class.java,
                            R.id.select_file_view, args.toBundle())
                }
            }
            R.id.content_add_file ->{
                args[SelectFileView.ARG_SELECT_FILE] = SelectFileView.SELECT_FILE
                if(showFolderOption){
                    findNavController().navigate(R.id.select_file_view, args.toBundle())
                }else{
                    navigateToPickEntityFromList(String::class.java,
                            R.id.select_file_view, args.toBundle())
                }
            }
            R.id.content_create_folder ->{
                args[ARG_LEAF] = false.toString()
                findNavController().navigate(R.id.content_entry_edit_dest, args.toBundle())
            }
        }


        if (isShowing != null && isShowing) {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        addLinkOptionView?.setOnClickListener(null)
        createFolderOptionView?.setOnClickListener(null)
        addGalleryOptionView?.setOnClickListener(null)
        addFileOptionView?.setOnClickListener(null)
        addLinkOptionView = null
        createFolderOptionView = null
        addFileOptionView = null
        addGalleryOptionView = null
    }

}
