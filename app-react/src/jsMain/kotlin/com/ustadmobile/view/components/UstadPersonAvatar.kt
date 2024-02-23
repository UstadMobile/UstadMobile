package com.ustadmobile.view.components

import com.ustadmobile.core.util.ext.initial
import com.ustadmobile.mui.components.UstadAvatar
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import web.cssom.Visibility

external interface UstadPersonAvatarProps: Props {

    var pictureUri: String?

    var personName: String?

    var colorName: String?

}


val UstadPersonAvatar = FC<UstadPersonAvatarProps> { props ->
    val personNameVal = props.personName
    UstadAvatar {
        colorName = props.colorName ?: props.personName
        sx {
            if(props.pictureUri == null && props.personName == null) {
                visibility = Visibility.hidden
            }
        }

        if(props.pictureUri != null) {
            src = props.pictureUri
        }else if(personNameVal != null) {
            + ReactNode(personNameVal.initial())
        }
    }
}
