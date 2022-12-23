package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.ErrorReportUiState
import com.ustadmobile.lib.db.entities.ErrorReport
import com.ustadmobile.mui.common.md
import com.ustadmobile.mui.common.xs
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.view.components.UstadBlankIcon
import csstype.TextAlign
import csstype.px
import kotlinx.css.JustifyContent
import kotlinx.css.span
import mui.icons.material.*
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import org.w3c.dom.AddEventListenerOptions
import react.FC
import react.Props
import react.create
import react.dom.aria.ariaLabel
import react.dom.aria.ariaValueText
import react.dom.html.ReactHTML.img

external interface ErrorReportProps: Props {
    var uiState: ErrorReportUiState
    var onTakeMeHomeClick: () -> Unit
    var onCopyIconClick: () -> Unit
    var onShareIconClick: () -> Unit
}

val ErrorReportComponent2 = FC<ErrorReportProps> { props ->

    val strings: StringsXml = useStringsXml()

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
                + strings[MessageID.sorry_something_went_wrong]
                variant = TypographyVariant.body2
            }

            Button {
                onClick = { props.onTakeMeHomeClick }
                variant = ButtonVariant.contained

                + strings[MessageID.take_me_home]
            }

            Divider { orientation = Orientation.horizontal }

            Stack{
                direction = responsive(StackDirection.row)
                spacing = responsive(20.px)

                sx {
                    justifyContent = csstype.JustifyContent.spaceBetween
                }

                Stack{
                    direction = responsive(StackDirection.column)

                    Typography {
                        + props.uiState.errorReport?.errUid.toString()
                        variant = TypographyVariant.body1
                    }

                    Typography {
                        + strings[MessageID.incident_id]
                        variant = TypographyVariant.body2
                    }
                }

                Stack{
                    direction = responsive(StackDirection.row)

                    IconButton{
                        ariaLabel = strings[MessageID.copy_code]
                        onClick = {
                            props.onCopyIconClick()
                        }

                        CopyAll{}
                    }

                    IconButton{
                        ariaLabel = strings[MessageID.share]
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
                    + strings[MessageID.error_code].replace("%1\$s",
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