package com.ustadmobile.view.clazz.inviteredeem

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazz.inviteredeem.ClazzInviteRedeemViewModel
import com.ustadmobile.core.viewmodel.clazz.inviteredeem.ClazzInviteRedeemUiState
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.util.ext.useCenterAlignGridContainer
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Grid
import mui.material.GridDirection
import mui.material.Stack
import mui.material.StackDirection
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import react.FC
import react.Props
import web.cssom.TextAlign
import mui.system.sx
import react.useRequiredContext
import web.cssom.AlignItems
import web.cssom.px
import mui.icons.material.InfoOutlined as InfoOutlinedIcon


external interface ClazzInviteRedeemProps : Props {
    var uiState: ClazzInviteRedeemUiState
    var processDecision: (Boolean) -> Unit
}

val ClazzInviteRedeemScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzInviteRedeemViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ClazzInviteRedeemUiState())

    ClazzInviteRedeemComponent2 {
        uiState = uiStateVal
        processDecision = viewModel::processDecision
    }
}

private val ClazzInviteRedeemComponent2 = FC<ClazzInviteRedeemProps> { props ->
    val strings = useStringProvider()
    val muiAppState = useMuiAppState()
    val errorStr = props.uiState.errorText
    val theme by useRequiredContext(ThemeContext)

    UstadStandardContainer {
        when {
            props.uiState.inviteUsed -> {
                Grid {
                    container = true
                    direction = responsive(GridDirection.column)

                    sx {
                        useCenterAlignGridContainer(muiAppState)
                    }

                    Grid {
                        item = true
                        InfoOutlinedIcon()
                    }

                    Grid {
                        item = true
                        + strings[MR.strings.invite_has_been_used]
                    }

                    if(errorStr != null) {
                        Grid {
                            item = true

                            Typography {
                                sx {
                                    color = theme.palette.error.main
                                }

                                variant = TypographyVariant.body1

                                + errorStr
                            }
                        }
                    }
                }
            }

            props.uiState.showButtons -> {
                Typography {
                    sx {
                        textAlign = TextAlign.center
                    }

                    +strings[MR.strings.do_you_want_to_join_this_course]
                }

                Stack {
                    direction = responsive(StackDirection.row)
                    spacing = responsive(10.px)

                    sx {
                        alignItems = AlignItems.center
                    }

                    Button {
                        onClick = { props.processDecision(true) }
                        variant = ButtonVariant.contained
                        disabled = !props.uiState.buttonsEnabled
                        +strings[MR.strings.accept].uppercase()
                    }

                    Button {
                        onClick = { props.processDecision(false) }
                        variant = ButtonVariant.outlined
                        disabled = !props.uiState.buttonsEnabled
                        +strings[MR.strings.decline].uppercase()
                    }
                }

                if(errorStr != null) {
                    Typography {
                        sx {
                            color = theme.palette.error.main
                        }

                        variant = TypographyVariant.body1

                        + errorStr
                    }
                }
            }
        }
    }
}
