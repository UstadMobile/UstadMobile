package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import csstype.px
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props

external interface RegisterAgeRedirectProps : Props {
    var onClickNext: () -> Unit
}

val RegisterAgeRedirectPreview = FC<Props> {
    RegisterAgeRedirectComponent2 {
    }
}

val RegisterAgeRedirectComponent2 = FC<RegisterAgeRedirectProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            Typography {
                variant = TypographyVariant.body1
                + strings[MessageID.what_is_your_date_of_birth]
            }

            Button {
                onClick = { props.onClickNext }
                variant = ButtonVariant.contained
                + strings[MessageID.next].uppercase()
            }
        }
    }
}