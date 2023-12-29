package com.ustadmobile.view.components

import com.ustadmobile.mui.components.ThemeContext
import web.cssom.*
import emotion.react.css
import js.core.jso
import mui.icons.material.AddAPhoto
import mui.material.*
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.input
import web.html.HTMLInputElement
import web.html.InputType
import web.url.URL

external interface UstadImageSelectButtonProps: Props {

    var imageUri: String?

    var onImageUriChanged: (String?) -> Unit

    var id: String?

}

val UstadImageSelectButton = FC<UstadImageSelectButtonProps> { props ->
    val inputRef = useRef<HTMLInputElement>(null)

    val theme by useRequiredContext(ThemeContext)

    Box {
        sx {
            justifyContent = JustifyContent.center
            alignItems = AlignItems.center
            display = Display.flex
        }

        input {
            type = InputType.file
            ref = inputRef
            id = props.id
            accept = ".jpg,.webp,.png,image/jpg,image/webp,image/png"
            css {
                asDynamic().display = "none"
            }

            onChange = {
                val file = it.target.files?.get(0)
                if(file != null) {
                    val url = URL.createObjectURL(file)
                    props.onImageUriChanged(url)
                }
            }
        }

        Badge {
            overlap = BadgeOverlap.circular
            anchorOrigin = jso {
                vertical = BadgeOriginVertical.bottom
                horizontal = BadgeOriginHorizontal.right
            }
            badgeContent = Avatar.create {
                onClick = {
                    inputRef.current?.click()
                }
                sx {
                    backgroundColor = theme.palette.secondary.main
                    height = 24.px
                    width = 24.px
                    cursor = Cursor.pointer
                }
                AddAPhoto {
                    sx {
                        height = 16.px
                        width = 16.px
                    }
                }
            }

            Avatar {
                src = props.imageUri
                onClick = {
                    inputRef.current?.click()
                }

                sx {
                    cursor = Cursor.pointer
                    height = 64.px
                    width = 64.px
                }
            }
        }
    }


}

val UstadImageSelectButtonPreview = FC<Props> {

    var imageUriState: String? by useState { null }

    UstadImageSelectButton {
        imageUri = imageUriState
        onImageUriChanged = {
            imageUriState = it
        }
    }

}
