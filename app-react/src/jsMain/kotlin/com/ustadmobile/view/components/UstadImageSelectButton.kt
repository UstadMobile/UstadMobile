package com.ustadmobile.view.components

import com.ustadmobile.mui.components.ThemeContext
import web.cssom.*
import emotion.react.css
import js.objects.jso
import mui.icons.material.AddAPhoto as AddAPhotoIcon
import mui.material.*
import mui.system.sx
import react.*
import react.dom.aria.ariaDisabled
import react.dom.html.ReactHTML.input
import web.html.HTMLInputElement
import web.html.InputType
import web.url.URL

external interface UstadImageSelectButtonProps: Props {

    var imageUri: String?

    var onImageUriChanged: (String?) -> Unit

    var id: String?

    var disabled: Boolean?

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

            //Note: if the value is not set then React doesn't recognize this as a controlled component
            // Components should not change between controlled and uncontrolled. We are just using the
            // input to get the file from the onChange event.
            value = ""

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

        if(props.imageUri != null) {
            Badge {
                overlap = BadgeOverlap.circular
                anchorOrigin = jso {
                    vertical = BadgeOriginVertical.bottom
                    horizontal = BadgeOriginHorizontal.right
                }
                badgeContent = Avatar.create {
                    if(props.disabled != true) {
                        onClick = {
                            inputRef.current?.click()
                        }
                    }

                    sx {
                        backgroundColor = theme.palette.secondary.main
                        height = 24.px
                        width = 24.px

                        if(props.disabled != true)
                            cursor = Cursor.pointer
                    }

                    AddAPhotoIcon {
                        sx {
                            height = 16.px
                            width = 16.px
                        }
                    }
                }

                ImageSelectButtonAvatar {
                    imageUri = props.imageUri
                    onClick = { inputRef.current?.click() }
                    disabled = props.disabled
                }
            }
        }else {
            ImageSelectButtonAvatar {
                imageUri = props.imageUri
                onClick = { inputRef.current?.click() }
                disabled = props.disabled
            }
        }
    }
}

private external interface ImageSelectButtonAvatarProps: Props {
    var onClick: () -> Unit
    var imageUri: String?
    var disabled: Boolean?
}

private val ImageSelectButtonAvatar = FC<ImageSelectButtonAvatarProps> { props ->
    Avatar {
        src = props.imageUri
        ariaDisabled = props.disabled == true
        if(props.disabled != true) {
            onClick = {
                props.onClick()
            }
        }


        sx {
            cursor = Cursor.pointer
            height = 64.px
            width = 64.px
        }

        if(props.imageUri == null) {
            AddAPhotoIcon()
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
