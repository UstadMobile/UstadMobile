package com.ustadmobile.port.android.view.clazzassignment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toughra.ustadmobile.databinding.ItemCommentNewSendBinding


class CommentsBottomSheet(
    val hintText: String,
    val personUid: Long,
    var onSubmitComment: ((String) -> Unit)?,
) : BottomSheetDialogFragment() {

    private var mBinding: ItemCommentNewSendBinding? = null

    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View
        mBinding = ItemCommentNewSendBinding.inflate(
            inflater, container, false
        ).also { binding ->
            rootView = binding.root
            binding.personUid = personUid
            binding.hintText = hintText
            binding.itemCommentNewCommentEt.requestFocus()
            val imm: InputMethodManager? = getSystemService(binding.root.context,
                    InputMethodManager::class.java)
            imm?.showSoftInput(binding.itemCommentNewCommentEt, InputMethodManager.SHOW_IMPLICIT)
            binding.itemCommentSubmitButton.setOnClickListener {
                val isShowing = this.dialog?.isShowing
                if (isShowing != null && isShowing) {
                    dismiss()
                }

                onSubmitComment?.invoke(binding.itemCommentNewCommentEt.text.toString())
            }
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
        mBinding?.itemCommentSubmitButton?.setOnClickListener(null)
        mBinding = null
        onSubmitComment = null
    }

}