package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzWorkWithSubmissionDetailBinding
import com.toughra.ustadmobile.databinding.ItemCommentNewSendBinding

class CommentsBottomSheet() : BottomSheetDialogFragment() {

    private var mBinding: ItemCommentNewSendBinding? = null
    private var rootView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = ItemCommentNewSendBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
        mBinding = null
    }

}