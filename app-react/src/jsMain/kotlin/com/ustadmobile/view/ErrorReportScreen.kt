package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.viewmodel.ErrorReportUiState
import com.ustadmobile.lib.db.entities.ErrorReport
import web.cssom.px
import mui.icons.material.CopyAll
import mui.icons.material.Share
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.img
import web.cssom.JustifyContent

external interface ErrorReportProps: Props {
    var uiState: ErrorReportUiState
    var onTakeMeHomeClick: () -> Unit
    var onCopyIconClick: () -> Unit
    var onShareIconClick: () -> Unit
}

val ErrorReportComponent2 = FC<ErrorReportProps> { props ->

    val strings: StringProvider = useStringProvider()

    Container{

        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            img{
                src = "img/undraw_access_denied_re_awnf.svg"
                alt = ""
                height = 300.0
            }

            Typography {
                + strings[MR.strings.sorry_something_went_wrong]
                variant = TypographyVariant.body2
            }

            Button {
                onClick = { props.onTakeMeHomeClick }
                variant = ButtonVariant.contained

                + strings[MR.strings.take_me_home]
            }

            Divider { orientation = Orientation.horizontal }

            Stack{
                direction = responsive(StackDirection.row)
                spacing = responsive(20.px)

                sx {
                    justifyContent = JustifyContent.spaceBetween
                }

                Stack{
                    direction = responsive(StackDirection.column)

                    Typography {
                        + props.uiState.errorReport?.errUid.toString()
                        variant = TypographyVariant.body1
                    }

                    Typography {
                        + strings[MR.strings.incident_id]
                        variant = TypographyVariant.body2
                    }
                }

                Stack{
                    direction = responsive(StackDirection.row)

                    IconButton{
                        ariaLabel = strings[MR.strings.copy_code]
                        onClick = {
                            props.onCopyIconClick()
                        }

                        CopyAll{}
                    }

                    IconButton{
                        ariaLabel = strings[MR.strings.share]
                        onClick = {
                            props.onShareIconClick()
                        }

                        Share{}
                    }
                }
            }

            Divider { orientation = Orientation.horizontal }

            Stack{
                direction = responsive(StackDirection.row)
                spacing = responsive(5.px)


                Typography {
                    + strings[MR.strings.error_code].replace("%1\$s",
                        props.uiState.errorReport?.errorCode.toString())
                    variant = TypographyVariant.body1
                }
            }

            Typography {
                + props.uiState.errorReport?.message.toString()
                variant = TypographyVariant.body2
            }
        }
    }

}

val ErrorReportPreview = FC<Props> {
    ErrorReportComponent2{
        uiState = ErrorReportUiState(
            errorReport = ErrorReport().apply {
                errorCode = 1234
                errUid = 1234123112
                message = "6x7 is the question when you think about it"
            }
        )
    }
}