package com.ustadmobile.libuicompose.util.ext

import androidx.compose.runtime.Composable
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.SortOrderOption
import dev.icerock.moko.resources.compose.stringResource

/**
 * Description of sort order option - used in both UstadListSortHeader and UstadSortOptionsBottomSheet
 */
@Composable
fun SortOrderOption.description() : String  {
    return buildString {
        append(stringResource(fieldMessageId))
        order?.also { orderVal ->
            append(" (")
            if(orderVal) {
                append(stringResource(MR.strings.ascending))
            }else {
                append(stringResource(MR.strings.descending))
            }
            append(")")
        }
    }
}