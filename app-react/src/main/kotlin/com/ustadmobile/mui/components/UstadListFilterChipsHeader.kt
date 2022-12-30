package com.ustadmobile.mui.components

import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.FilterChipsHeaderUiState
import csstype.Margin
import csstype.px
import mui.material.*
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode

external interface UstadListFilterChipsHeaderProps: Props {

    var uiState: FilterChipsHeaderUiState

    var onClickFilterChip: (MessageIdOption2) -> Unit

}

val UstadListFilterChipsHeader = FC<UstadListFilterChipsHeaderProps> { props ->

    val strings = useStringsXml()

    List {

        props.uiState.filterOptions.forEach { filterOption ->
            Chip {
                clickable = props.uiState.fieldsEnabled

                sx {
                    margin = Margin(horizontal = 5.px, vertical = 0.px)
                }

                onClick = {
                    props.onClickFilterChip(filterOption)
                }
                label = ReactNode(strings[filterOption.messageId])
            }
        }
    }
}

val UstadListFilterChipsHeaderPreview = FC<Props> {
    UstadListFilterChipsHeader {
        uiState = FilterChipsHeaderUiState(
            filterOptions = listOf(
                MessageIdOption2(MessageID.currently_enrolled, ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED),
                MessageIdOption2(MessageID.past_enrollments, ClazzDaoCommon.FILTER_PAST_ENROLLMENTS),
                MessageIdOption2(MessageID.all, 0)
            )
        )
    }
}