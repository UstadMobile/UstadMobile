package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.ContentEntryAddOptionsView
import kotlinx.android.synthetic.main.fragment_content_entry_add_options.view.*

/**
 * Fragment class responsible for content creation selection, you can create content by one of the following
 * CONTENT_CREATE_FOLDER = Create new content category
 * CONTENT_IMPORT_FILE = Create content from file (epub, h5p e.t.c)
 * CONTENT_CREATE_CONTENT = create content from out content editor
 * CONTENT_IMPORT_LINK = Create content from a link
 */

class ContentEntryAddOptionsFragment : UstadBottomSheetFragment(), ContentEntryAddOptionsView, View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_content_entry_add_options, container, false)
        rootView.content_create_folder.setOnClickListener(this)
        rootView.content_import_file.setOnClickListener(this)
        rootView.content_import_link.setOnClickListener(this)
        return rootView;
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.content_create_folder -> {}
            R.id.content_import_file -> {}
            R.id.content_import_link -> {}
        }
    }

    override val viewContext: Any
        get() = requireActivity()

}
