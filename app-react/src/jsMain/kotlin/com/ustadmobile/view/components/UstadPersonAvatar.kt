package com.ustadmobile.view.components

import mui.material.Avatar
import react.FC
import react.Props

external interface UstadPersonAvatarProps: Props {

    var personUid: Long

    var pictureUri: String?

}

val UstadPersonAvatar = FC<UstadPersonAvatarProps> { props ->
    Avatar {
        src = props.pictureUri
    }

}
