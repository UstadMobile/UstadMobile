package com.ustadmobile.mui.components

import com.ustadmobile.core.util.avatarColorForName
import com.ustadmobile.core.util.ext.rgbColorProperty
import com.ustadmobile.util.ext.assignPropsTo
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
        //Assign all properties except colorName - which is our own property and would not be
        //recognized by the Avatar component (leads to a nasty/verbose warning message on console)
        props.assignPropsTo(
            receiver = this,
            filter = { it != "colorName" }
        )

        sx {
            + props.sx
            backgroundColor = bgColor
        }

        + props.children
    }
}


