package com.ustadmobile.mui.components

import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import react.FC
import mui.icons.material.Sync as SyncIcon
import mui.icons.material.Schedule as ScheduleIcon
import mui.icons.material.Error as ErrorIcon
import mui.icons.material.DownloadDone as DownloadDoneIcon
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import mui.material.SvgIconProps
import react.create
import react.dom.aria.ariaLabel

external interface UstadTransferStatusIconProps: SvgIconProps {
    var transferJobItemStatus: TransferJobItemStatus
}

val UstadTransferStatusIcon = FC<UstadTransferStatusIconProps> {props ->
    val strings = useStringProvider()
    val (icon, stringResource) = when(props.transferJobItemStatus) {
        TransferJobItemStatus.IN_PROGRESS -> SyncIcon to MR.strings.in_progress
        TransferJobItemStatus.QUEUED -> ScheduleIcon to MR.strings.queued
        TransferJobItemStatus.FAILED -> ErrorIcon to MR.strings.failed
        TransferJobItemStatus.COMPLETE -> DownloadDoneIcon to MR.strings.completed
    }

    +icon.create {
        + props
        ariaLabel = strings[stringResource]
    }
}

