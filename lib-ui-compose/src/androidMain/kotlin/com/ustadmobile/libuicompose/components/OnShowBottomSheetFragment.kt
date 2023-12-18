package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ustadmobile.libuicompose.util.ext.getContextSupportFragmentManager
import com.ustadmobile.libuicompose.view.clazzassignment.BottomSheetDialogContent
import com.ustadmobile.libuicompose.view.clazzassignment.CommentBottomSheetFragment


@Composable
actual fun onShowBottomSheetFragmentFunction(
    content: BottomSheetDialogContent,
): () -> Unit {
    val context = LocalContext.current

    return {
        CommentBottomSheetFragment(
            content = content
        ).show(context.getContextSupportFragmentManager(), "sheet")
    }
}
