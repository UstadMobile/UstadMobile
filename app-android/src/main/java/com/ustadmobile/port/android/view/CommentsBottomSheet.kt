package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.DefaultNewCommentItemListener

class CommentsBottomSheet(val adapter: NewCommentRecyclerViewAdapter?) : BottomSheetDialogFragment(){

    private var mRecyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mRecyclerView = inflater.inflate(R.layout.fragment_options_bottom_sheet, container, false) as RecyclerView

        mRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        adapter?.isBottomSheet = true
        adapter?.newOpenSheetListener = null
        mRecyclerView?.adapter = adapter


        return mRecyclerView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mRecyclerView?.adapter = null
        mRecyclerView = null
    }

}