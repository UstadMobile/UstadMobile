package com.ustadmobile.mui.components

import com.ustadmobile.core.util.avatarColorForName
import com.ustadmobile.core.util.ext.rgbColorProperty
import mui.material.Avatar
import mui.material.AvatarProps
import mui.system.sx
import react.FC
import react.useMemo
import web.cssom.Color

external interface UstadAvatarProps: AvatarProps {

    var colorName: String?

}

val UstadAvatar = FC<UstadAvatarProps> { props ->
    val colorNameVal = props.colorName
    val pictureUriVal = props.src
    val bgColor = useMemo(pictureUriVal, colorNameVal) {
        if(pictureUriVal == null && colorNameVal != null) {
            avatarColorForName(colorNameVal).rgbColorProperty()
        }else {
            Color("#ffffff00")
        }
    }

    Avatar {
        + props

        sx {
            + props.sx
            backgroundColor = bgColor
        }

        + props.children
    }
}


