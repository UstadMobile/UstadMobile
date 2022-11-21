package com.ustadmobile.view

import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.SiteTermsDetailUiState
import com.ustadmobile.lib.db.entities.SiteTerms
import csstype.px
import kotlinx.js.jso
import mui.material.Container
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useState

external interface SiteTermsDetailScreenProps : Props {

    var uiState: SiteTermsDetailUiState

}

val SiteTermsDetailScreenComponent2 = FC<SiteTermsDetailScreenProps> { props ->

    Container {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            ReactHTML.div {
                dangerouslySetInnerHTML = jso {
                    __html = props.uiState.siteTerms?.termsHtml ?: ""
                }
            }
        }
    }
}

val SiteTermsDetailScreenPreview = FC<Props> {

    val uiStateVar : SiteTermsDetailUiState by useState {
        SiteTermsDetailUiState(
            siteTerms = SiteTerms().apply {
                termsHtml = "<h1>This is a Heading</h1>\n" +
                        "<p>This is a paragraph.</p>"
            }
        )
    }

    SiteTermsDetailScreenComponent2 {
        uiState = uiStateVar
    }
}