package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.MR

@Composable
@Preview
private fun UstadListSortHeaderPreview() {
    UstadListSortHeader(
        activeSortOrderOption = SortOrderOption(
            MR.strings.name_key,
            ClazzDaoCommon.SORT_CLAZZNAME_ASC,
            true
        ))
}