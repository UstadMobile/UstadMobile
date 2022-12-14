package com.ustadmobile.view

import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.themeContext
import com.ustadmobile.mui.components.umLinearProgress
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignEndItems
import com.ustadmobile.util.StyleManager.partnersList
import com.ustadmobile.util.StyleManager.splashComponentContainer
import com.ustadmobile.util.ThemeManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umPartner
import kotlinx.css.FlexDirection
import kotlinx.css.LinearDimension
import kotlinx.css.width
import mui.material.LinearProgressColor
import react.RBuilder
import react.RComponent
import styled.css
import styled.styledDiv
import styled.styledImg

class SplashComponent (props: UmProps): RComponent<UmProps, UmState>(props) {

    override fun RBuilder.render() {
        themeContext.Consumer { _ ->
            styledDiv {
                css (splashComponentContainer)

                styledDiv {
                    css(StyleManager.splashComponentPreloadContainer)
                    umGridContainer {
                        css(StyleManager.alignCenterItems)
                        umItem(GridSize.cells12) {
                            css(StyleManager.alignCenterItems)
                            styledImg {
                                css{
                                    width = LinearDimension("90%")
                                }
                                attrs.src = "assets/logo.png"
                            }
                        }

                        umItem(GridSize.cells12) {
                            css(StyleManager.alignCenterItems)
                            val color = when {
                                ThemeManager.isDarkModeActive() -> LinearProgressColor.secondary
                                else -> LinearProgressColor.primary
                            }

                            umLinearProgress(color = color) {
                                css {
                                    width = LinearDimension("100%")
                                }
                            }
                        }
                    }
                }

                umItem(GridSize.cells12, flexDirection = FlexDirection.rowReverse) {
                    css{
                        +partnersList
                        +alignEndItems
                    }
                    umPartner("tajik_emblem.webp")
                    umPartner("unicef_tj.webp")
                    umPartner("Eu_logo_tg.webp")
                }
            }
        }
    }
}
fun RBuilder.renderSplashComponent() = child(SplashComponent::class) {}
