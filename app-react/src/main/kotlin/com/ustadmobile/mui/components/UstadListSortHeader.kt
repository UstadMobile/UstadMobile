package com.ustadmobile.mui.components

import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.UstadListSortHeaderUiState
import mui.icons.material.ArrowDownward
import mui.icons.material.ArrowUpward
import mui.material.*
import react.FC
import react.Props
import react.create

external interface UstadListSortHeaderProps : Props {

    var uiState: UstadListSortHeaderUiState

    var onClickSort: Unit

}

val UstadListSortHeader = FC<UstadListSortHeaderProps> { props ->

    val strings = useStringsXml()

    val sortIcon = if(props.uiState.sortOption?.order == true)
        ArrowDownward.create()
    else
        ArrowUpward.create()

    Stack {
        onClick = { props.onClickSort }

        Typography {
            + strings[props.uiState.sortOption?.fieldMessageId
                ?: MessageID.field_person_age]
        }

        Icon {
            + sortIcon
        }
    }
}

val UstadListSortHeaderPreview = FC<Props> {

    UstadListSortHeader {
        uiState = UstadListSortHeaderUiState(
            sortOption = SortOrderOption(
                MessageID.name,
                ClazzDaoCommon.SORT_CLAZZNAME_ASC,
                true
            )
        )
    }
}
