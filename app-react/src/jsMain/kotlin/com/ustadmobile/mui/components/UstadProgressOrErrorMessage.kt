package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.util.ext.useCenterAlignGridContainer
import mui.icons.material.ErrorOutline
import mui.material.CircularProgress
import mui.material.CircularProgressVariant
import mui.material.Grid
import mui.material.GridDirection
import mui.material.SvgIconSize
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import web.cssom.PaddingLeft

external interface UstadProgressOrErrorMessageProps: Props {

    var errorMessage: String?

    var progress: Int?

    var processedBytes: Long?

    var totalBytes: Long?
}

val UstadProgressOrErrorMessage = FC<UstadProgressOrErrorMessageProps> { props ->
    val muiAppState = useMuiAppState()
    val strings = useStringProvider()

    Grid {
        container = true
        direction = responsive(GridDirection.column)
        sx {
            useCenterAlignGridContainer(muiAppState)
        }

        val errorStr = props.errorMessage

        if(errorStr == null) {
            Grid {
                item = true

                CircularProgress {
                    sx {
                        paddingLeft = "auto".unsafeCast<PaddingLeft>()
                        paddingRight = "auto".unsafeCast<PaddingLeft>()
                    }
                    variant = if(props.progress != null) {
                        CircularProgressVariant.indeterminate
                    }else {
                        CircularProgressVariant.determinate
                    }
                    props.progress?.also {
                        value = it
                    }
                }
            }

            Grid {
                item = true

                + "${strings[MR.strings.uploading]}: "

                props.processedBytes?.also {
                    + "${UMFileUtil.formatFileSizeMb(it)} /"
                }

                props.totalBytes?.also {
                    + "${UMFileUtil.formatFileSizeMb(it)} "
                }
            }
        }else {
            Grid {
                item = true

                ErrorOutline {
                    fontSize = SvgIconSize.large
                }
            }

            Grid {
                item = true

                + errorStr
            }
        }

    }
}
