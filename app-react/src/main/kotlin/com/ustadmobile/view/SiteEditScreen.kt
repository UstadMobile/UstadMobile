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
import mui.icons.material.*
import mui.material.*
import mui.material.List
import mui.material.styles.TypographyVariant
import mui.system.Spacing
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import org.kodein.di.bindings.WithContext
import react.FC
import react.Props
import react.create
import react.dom.onChange

external interface SiteEditProps: Props {
    var uiState: SiteEditUiState
    var onSiteChanged: (Site?) -> Unit
    var onClickLang: (SiteTermsWithLanguage) -> Unit
    var onDeleteClick: (SiteTermsWithLanguage) -> Unit
    var onClickAddItem: () -> Unit
}

private interface CustomSwitchProps: Props {
    var label: String
    var checked: Boolean
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
                    props.onSiteChanged(
                        props.uiState.site?.shallowCopy {
                            siteName = it
                        })
                }
            }

            CustomSwitch{
                label = strings[MessageID.guest_login_enabled]
                checked = props.uiState.site?.guestLogin ?: true
            }

            CustomSwitch{
                label = strings[MessageID.registration_allowed]
                checked = props.uiState.site?.registrationAllowed ?: true
            }

            Typography {
                variant = TypographyVariant.h6
                + strings[MessageID.terms_and_policies]
            }

            ListItem {

                onClick = {
                    props.onClickAddItem()
                }

                ListItemIcon {
                    Add{}
                }

                ListItemText {
                    + (strings[MessageID.terms_and_policies])
                }
            }

            List{
                props.uiState.siteTerms.forEach { item ->
                    ListItem {

                        onClick = {
                            props.onClickLang(item)
                        }
                        ListItemText {
                            + (item.stLanguage?.name ?: "")
                        }

                        secondaryAction = IconButton.create {
                            onClick = {
                                props.onDeleteClick(item)
                            }
                            Delete {}
                        }
                    }
                }
            }
        }
    }

}

private var CustomSwitch = FC<CustomSwitchProps> { props ->

    Stack{
        sx{
            width = width
        }

        direction = responsive(mui.material.StackDirection.row)
        spacing = responsive(100.px)

        Typography {
            variant = TypographyVariant.h6
            + props.label
        }

        Switch{
            defaultChecked = props.checked
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