package com.ustadmobile.view.components

import com.ustadmobile.core.util.avatarColorForName
import com.ustadmobile.core.util.ext.initial
import com.ustadmobile.core.util.ext.rgbColorProperty
import mui.material.Avatar
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.useMemo
import web.cssom.Color
import web.cssom.Visibility

external interface UstadPersonAvatarProps: Props {

    var personUid: Long

    var pictureUri: String?

    var personName: String?

    var colorName: String?

}


val UstadPersonAvatar = FC<UstadPersonAvatarProps> { props ->
    val pictureUriVal = props.pictureUri
    val personNameVal = props.personName
    val colorNameVal = props.colorName ?: props.personName
    val bgColor = useMemo(pictureUriVal, colorNameVal) {
        if(pictureUriVal == null && colorNameVal != null) {
            avatarColorForName(colorNameVal).rgbColorProperty()
        }else {
            Color("#ffffff00")
        }
    }

    Avatar {
        sx {
            if(pictureUriVal == null && personNameVal == null) {
                visibility = Visibility.hidden
            }
        }
        if(pictureUriVal != null) {
            src = props.pictureUri
        }else if(personNameVal != null) {
            sx {
                backgroundColor = bgColor
            }

            + ReactNode(personNameVal.initial())
        }
    }
}
