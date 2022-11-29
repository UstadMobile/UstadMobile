package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.SiteEditUiState
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadTextEditField
import csstype.px
import kotlinx.css.span
import mui.icons.material.AccountBalanceRounded
import mui.icons.material.WidthFull
import mui.material.Container
import mui.material.Stack
import mui.material.Switch
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.Spacing
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import org.kodein.di.bindings.WithContext
import react.FC
import react.Props
import react.create

external interface SiteEditProps: Props {
    var uiState: SiteEditUiState
    var onSiteNameChanged: (Site?) -> Unit
    var onGuestLoginEnabledChanged: (Boolean) -> Unit
    var onRegistrationAllowedChanged: (Boolean) -> Unit
    var onAddButtonClicked: () -> Unit
}

val SiteEditComponent2 = FC<SiteEditProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack{
            direction = responsive(mui.material.StackDirection.column)
            spacing = responsive(10.px)

            UstadTextEditField {
                value = props.uiState.site?.siteName ?: ""
                label = strings[MessageID.first_names]
                error = props.uiState.siteNameError
                enabled = props.uiState.fieldsEnabled
                onChange = {
//                    props.onPersonChanged(
//                        props.uiState.person?.shallowCopy {
//                            firstNames = it
//                        })
                }
            }
        }
    }

    Container{
        maxWidth = "lg"

        Stack{
            sx{
                width = width
            }
            direction = responsive(mui.material.StackDirection.row)
            spacing = responsive(600.px)

            Typography {
                variant = TypographyVariant.h6
                + strings[MessageID.terms_and_policies]
            }

            Switch{
                defaultChecked = props.uiState.site?.guestLogin
            }
        }
    }

}

val SiteEditPreview = FC<Props> {
    SiteEditComponent2{
        uiState = SiteEditUiState(
            site = Site().apply {
                siteName = "My Site"
            },
            siteTerms = listOf(
                SiteTermsWithLanguage().apply {
                    stLanguage = Language().apply {
                        name = "fa"
                    }
                }
            )
        )
    }
}