package com.ustadmobile.mui.components

import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.SortOrderOption
import csstype.JustifyContent
import csstype.pct
import csstype.px
import mui.icons.material.ArrowDownward
import mui.icons.material.ArrowUpward
import mui.material.*
import mui.system.sx
import react.FC
import react.Props
import react.create

external interface UstadListSortHeaderProps : Props {

    var activeSortOrderOption: SortOrderOption

    var enabled: Boolean?

    var onClickSort: () -> Unit

}

val UstadListSortHeader = FC<UstadListSortHeaderProps> { props ->

    val strings = useStringsXml()

    val sortIcon = if(props.activeSortOrderOption.order)
        ArrowDownward.create()
    else
        ArrowUpward.create()

    ButtonBase {
        sx {
            justifyContent = JustifyContent.start
            width = 100.pct
            padding = 16.px
        }
        disabled = props.enabled == false

        onClick = { props.onClickSort }

        Typography {
            + strings[props.activeSortOrderOption.fieldMessageId]
        }

        Icon {
            + sortIcon
        }
    }
}

val UstadListSortHeaderPreview = FC<Props> {

    UstadListSortHeader {
        activeSortOrderOption = SortOrderOption(
                MessageID.name,
                ClazzDaoCommon.SORT_CLAZZNAME_ASC,
                true
            )
        enabled = true
        onClickSort = { }
    }
}
