package com.ustadmobile.mui.components

import com.ustadmobile.core.util.avatarColorForName
import com.ustadmobile.core.util.ext.rgbColorProperty
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import mui.material.Box
import mui.system.sx
import react.FC
import react.Props
import react.useMemo
import web.cssom.px

external interface UstadBlockIconProps: Props {

    var title: String

    var courseBlock: CourseBlock?

    var contentEntry: ContentEntry?

    var pictureUri: String?

}


val UstadBlockIcon = FC<UstadBlockIconProps> { props ->
    val bgColor = useMemo(props.title){
        avatarColorForName(props.title).rgbColorProperty()
    }

    Box {
        sx {
            backgroundColor = bgColor
            width = 40.px
            height = 40.px
        }
    }



}
