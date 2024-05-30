package com.ustadmobile.mui.components

import com.ustadmobile.lib.db.composites.BlockStatus
import js.objects.jso
import mui.material.Avatar
import mui.material.Badge
import mui.material.BadgeOriginHorizontal
import mui.material.BadgeOriginVertical
import mui.material.LinearProgress
import mui.material.LinearProgressVariant
import mui.system.Box
import mui.system.PropsWithSx
import mui.system.sx
import react.FC
import react.create
import react.useRequiredContext
import web.cssom.pct
import web.cssom.px
import mui.icons.material.Close as CloseIcon
import mui.icons.material.Check as CheckIcon

external interface UstadBlockStatusProgressBarProps: PropsWithSx {
    var blockStatus: BlockStatus?
}

val UstadBlockStatusProgressBar = FC<UstadBlockStatusProgressBarProps> { props ->
    val theme by useRequiredContext(ThemeContext)

    Box {
        sx = props.sx

        val hasIcon = props.blockStatus?.sIsCompleted == true ||
                props.blockStatus?.sIsSuccess != null

        Badge {
            invisible = !hasIcon
            anchorOrigin = jso {
                vertical = BadgeOriginVertical.bottom
                horizontal = BadgeOriginHorizontal.left
            }

            sx {
                width = 100.pct
            }

            badgeContent = if(hasIcon) {
                Avatar.create {

                    sx {
                        backgroundColor = if(
                            props.blockStatus?.sIsCompleted == true &&
                            props.blockStatus?.sIsSuccess == false
                        ) {
                            theme.palette.error.main
                        }else {
                            theme.palette.success.main
                        }

                        height = 16.px
                        width = 16.px
                    }

                    if(props.blockStatus?.sIsCompleted == true && props.blockStatus?.sIsSuccess == false) {
                        CloseIcon {
                            //Failed
                            sx {
                                height = 8.px
                                width = 8.px
                            }
                        }
                    }else if(props.blockStatus?.sIsCompleted == true){
                        CheckIcon {
                            //Passed
                            sx {
                                height = 8.px
                                width = 8.px
                            }
                        }
                    }
                }
            }else {
                null
            }

            props.blockStatus?.sProgress?.also { progressVal ->
                LinearProgress {
                    sx {
                        width = 100.pct
                        height = 4.px
                    }
                    variant = LinearProgressVariant.determinate
                    value = progressVal
                }
            }

        }



    }
}
