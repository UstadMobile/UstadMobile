package com.ustadmobile.mui.components

import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.MessageIdOption2
import web.cssom.Margin
import web.cssom.px
import mui.material.*
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode

external interface UstadListFilterChipsHeaderProps: Props {

    var onClickFilterChip: (MessageIdOption2) -> Unit

    var filterOptions: List<MessageIdOption2>

    var selectedChipId: Int

    var enabled: Boolean?

}

val UstadListFilterChipsHeader = FC<UstadListFilterChipsHeaderProps> { props ->

    val strings = useStringsXml()

    Box {
        props.filterOptions.forEach { filterOption ->
            Chip {
                disabled = (props.enabled == false)
                variant = if(filterOption.value == props.selectedChipId) {
                    ChipVariant.filled
                }else {
                    ChipVariant.outlined
                }

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
        onClickFilterChip = { }
        filterOptions = listOf(
            MessageIdOption2(MessageID.currently_enrolled, ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED),
            MessageIdOption2(MessageID.past_enrollments, ClazzDaoCommon.FILTER_PAST_ENROLLMENTS),
            MessageIdOption2(MessageID.all, 0)
        )
        selectedChipId = ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED


    }
}