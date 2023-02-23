package com.ustadmobile.mui.components

import csstype.*
import emotion.react.css
import js.core.jso
import mui.icons.material.Search
import mui.material.InputBase
import mui.system.Breakpoint
import mui.system.sx
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useContext
import web.html.HTMLInputElement

external interface AppBarSearchProps: Props {
    var searchText: String
    var onTextChanged: (String) -> Unit
}

/**
 * Search to display within the AppBar, roughly as per
 *  https://mui.com/material-ui/react-app-bar/#app-bar-with-search-field
 *
 *
 * Seems like there aren't wrappers for MUI styled components yet. We could use styled for standard
 * components, but that wouldn't give us access to the MUI theme.
 *
 * e.g.
 * val StyledSearch = styled(div, jso { })() { props ->
 *   jso {
 *      position = Position.relative
 *   }
 * }
 */
val AppBarSearch = FC<AppBarSearchProps> {props ->
    val theme by useContext(ThemeContext)

    div {
        css {
            position = Position.relative
            borderRadius = theme.shape.borderRadius
            backgroundColor = rgba(255, 255, 255, 0.15)
            marginLeft = 0.px
            width = Auto.auto
        }

        div {
            css {
                padding = theme.spacing(0, 2)
                height = 100.pct
                position = Position.absolute
                display = Display.flex
                alignItems = AlignItems.center
                justifyContent = JustifyContent.center
            }

            //Icon
            Search { }
        }


        InputBase {
            placeholder = "Search..."
            value = props.searchText
            sx {
                color = Globals.inherit
                padding = theme.spacing(1, 1, 1, 1)
                transition = theme.transitions.create(arrayOf("width"), jso { })
                asDynamic().paddingLeft = "calc(1em + ${theme.spacing(4)})"

                asDynamic()[theme.breakpoints.up(Breakpoint.sm)] =
                    jso<PropertiesBuilder> {
                        width = 24.ch
                        asDynamic()["&:focus"] = jso<PropertiesBuilder> {
                            width = 38.ch
                        }
                    }
            }

            onChange = {
                props.onTextChanged((it.target as HTMLInputElement).value)
            }
        }
    }
}