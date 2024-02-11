package com.ustadmobile.mui.ext

import com.ustadmobile.core.impl.appstate.AppActionButton
import com.ustadmobile.core.impl.appstate.AppStateIcon
import mui.icons.material.SvgIconComponent
import mui.icons.material.Close as CloseIcon
import mui.icons.material.DriveFileMove as DriveFileMoveIcon

val AppActionButton.iconComponent: SvgIconComponent
    get() = when(this.icon) {
        AppStateIcon.CLOSE -> CloseIcon
        AppStateIcon.MOVE -> DriveFileMoveIcon
    }
