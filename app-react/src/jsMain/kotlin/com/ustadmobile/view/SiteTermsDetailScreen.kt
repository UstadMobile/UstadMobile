package com.ustadmobile.view

import com.ustadmobile.core.viewmodel.site.termsdetail.SiteTermsDetailUiState
import com.ustadmobile.lib.db.entities.SiteTerms
import web.cssom.px
import js.objects.jso
import mui.material.Container
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useMemo
import react.useState

external interface SiteTermsDetailScreenProps : Props {

    var uiState: SiteTermsDetailUiState

}

val SiteTermsDetailScreenComponent2 = FC<SiteTermsDetailScreenProps> { props ->
    val termsCleanHtml = useMemo(props.uiState.siteTerms?.termsHtml ?: "") {
        props.uiState.siteTerms?.termsHtml ?: ""
    }

    Container {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            div {
                dangerouslySetInnerHTML = jso {
                    __html = termsCleanHtml
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