package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toughra.ustadmobile.databinding.ItemCommentNewSendBinding

class CommentsBottomSheet(val publicComment: Boolean, val hintText: String, val personUid: Long) : BottomSheetDialogFragment() {

    private var mBinding: ItemCommentNewSendBinding? = null
    private var rootView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = ItemCommentNewSendBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
            it.personUid = personUid
            it.hintText = hintText
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
        mBinding = null
    }

}