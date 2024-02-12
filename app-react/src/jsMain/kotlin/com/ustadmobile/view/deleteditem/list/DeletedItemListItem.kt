package com.ustadmobile.view.deleteditem.list

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.deleteditem.delItemContentTypeStringResource
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.DeletedItem
import kotlinx.datetime.TimeZone
import mui.material.IconButton
import mui.material.ListItem
import mui.material.ListItemIcon
import mui.material.ListItemSecondaryAction
import mui.material.ListItemText
import mui.material.Tooltip
import react.FC
import react.Props
import react.ReactNode
import react.dom.aria.ariaLabel
import mui.icons.material.Restore as RestoreIcon
import mui.icons.material.DeleteForever as DeleteForeverIcon
import mui.icons.material.InsertDriveFile as InsertDriveFileIcon
import mui.icons.material.Folder as FolderIcon

external interface DeletedItemListItemProps: Props {
    var item: DeletedItem?

    var onClickDeletePermanently: (DeletedItem) -> Unit

    var onClickRestore: (DeletedItem) -> Unit

}

val DeletedItemListItem = FC<DeletedItemListItemProps> { props ->
    val strings = useStringProvider()
    val deletedDateFormatted = useFormattedDateAndTime(
        timeInMillis = props.item?.delItemTimeDeleted ?: 0,
        timezoneId = TimeZone.currentSystemDefault().id,
    )

    ListItem {
        ListItemIcon {
            if(props.item?.delItemIsFolder == true) {
                FolderIcon()
            }else {
                InsertDriveFileIcon()
            }
        }

        ListItemText {
            primary = ReactNode(props.item?.delItemName ?: "")
            secondary = ReactNode(buildString {
                props.item?.delItemContentTypeStringResource?.also {
                    append("${strings[MR.strings.type]}: ${strings[it]} ")
                }

                append("${strings[MR.strings.deleted]}: $deletedDateFormatted")
            })
        }

        ListItemSecondaryAction {
            Tooltip {
                title = ReactNode(strings[MR.strings.restore])

                IconButton {
                    ariaLabel = strings[MR.strings.restore]

                    onClick = {
                        props.item?.also(props.onClickRestore)
                    }

                    RestoreIcon()
                }
            }

            Tooltip {
                title = ReactNode(strings[MR.strings.delete_permanently])

                IconButton {
                    ariaLabel = strings[MR.strings.delete_permanently]
                    onClick = {
                        props.item?.also(props.onClickDeletePermanently)
                    }

                    DeleteForeverIcon()
                }
            }
        }
    }

}