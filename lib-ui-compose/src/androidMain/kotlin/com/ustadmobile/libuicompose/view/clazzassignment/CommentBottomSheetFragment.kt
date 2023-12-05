package com.ustadmobile.libuicompose.view.clazzassignment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


/**
 * The BottomSheet and IME padding on Jetpack compose when the soft keyboard is visible does not work
 * as per https://issuetracker.google.com/issues/308308431
 *
 * Therefor this is a fragment on Android
 */
class CommentBottomSheetFragment(
    content: BottomSheetDialogContent,
): BottomSheetDialogFragment() {

    private var content: BottomSheetDialogContent? = content

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        return ComposeView(requireContext()).apply {
            setContent {
                content?.invoke { dismiss() }
            }
        }

    }

    override fun onDestroyView() {
        content = null
        super.onDestroyView()
    }
}