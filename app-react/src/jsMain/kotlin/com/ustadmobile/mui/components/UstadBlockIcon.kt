package com.ustadmobile.mui.components

import com.ustadmobile.core.util.avatarColorForName
import com.ustadmobile.core.util.ext.rgbColorProperty
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.view.clazz.iconComponent
import com.ustadmobile.view.contententry.contentTypeIconComponent
import emotion.react.css
import mui.material.Box
import mui.material.SvgIconSize
import mui.system.sx
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.img
import react.useMemo
import react.useRequiredContext
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.Height
import web.cssom.JustifyContent
import web.cssom.ObjectFit
import web.cssom.Width
import web.cssom.px

external interface UstadBlockIconProps: Props {

    var title: String

    var courseBlock: CourseBlock?

    var contentEntry: ContentEntry?

    var pictureUri: String?

    var width: Width?

    var height: Height?

    var iconSize: SvgIconSize?

}


val UstadBlockIcon = FC<UstadBlockIconProps> { props ->
    val bgColor = useMemo(props.title){
        avatarColorForName(props.title).rgbColorProperty()
    }

    val theme by useRequiredContext(ThemeContext)

    val contentEntryVal = props.contentEntry
    val courseBlockVal = props.courseBlock
    val pictureUriVal = props.pictureUri

    Box {
        sx {
            if(props.pictureUri == null && props.title.isNotEmpty())
                backgroundColor = bgColor

            width = props.width ?: 40.px
            height = props.height ?: 40.px
            display = Display.flex
            justifyContent = JustifyContent.center
            alignItems  = AlignItems.center
        }

        if(pictureUriVal != null) {
            img {
                css {
                    objectFit = ObjectFit.scaleDown
                    height = props.height ?: 40.px
                    width = props.width ?: 40.px
                    display = Display.block
                }

                src = pictureUriVal
            }
        }else {
            val iconComponent = contentEntryVal?.contentTypeIconComponent()
                ?: courseBlockVal?.iconComponent()
            if(iconComponent != null) {
                + iconComponent.create {
                    sx {
                        color = theme.palette.primary.contrastText
                    }

                    props.iconSize?.also {
                        fontSize = it
                    }
                }
            }
        }
    }

}
