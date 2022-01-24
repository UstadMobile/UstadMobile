package com.ustadmobile.port.android.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toughra.ustadmobile.databinding.ItemCommentNewSendBinding
import com.ustadmobile.core.controller.NewCommentItemListener


class CommentsBottomSheet(val publicComment: Boolean, val hintText: String,
                          val personUid: Long, var listener: NewCommentItemListener?) : BottomSheetDialogFragment() {

    private var mBinding: ItemCommentNewSendBinding? = null
    private var rootView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = ItemCommentNewSendBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
            it.sheet = this
            it.personUid = personUid
            it.hintText = hintText
            it.listener = listener
            it.itemCommentNewCommentEt.requestFocus()
            val imm: InputMethodManager? = getSystemService(it.root.context,
                    InputMethodManager::class.java)
            imm?.showSoftInput(it.itemCommentNewCommentEt, InputMethodManager.SHOW_IMPLICIT)
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
        mBinding = null
        listener = null
    }

}