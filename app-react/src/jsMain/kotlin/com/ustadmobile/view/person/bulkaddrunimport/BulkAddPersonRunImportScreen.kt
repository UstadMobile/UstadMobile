package com.ustadmobile.view.person.bulkaddrunimport

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.person.bulkaddrunimport.BulkAddPersonRunImportUiState
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.mui.components.UstadStandardContainer
import mui.material.Grid
import mui.material.GridDirection
import mui.material.LinearProgress
import mui.material.LinearProgressVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.person.bulkaddrunimport.BulkAddPersonRunImportViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.util.ext.useCenterAlignGridContainer
import web.cssom.px
import mui.material.List
import mui.material.ListItem
import mui.material.ListItemIcon
import mui.material.ListItemText
import react.ReactNode
import mui.icons.material.ErrorOutline as ErrorOutlineIcon
import mui.icons.material.Check as CheckIcon

external interface BulkAddPersonRunImportProps : Props {

    var uiState: BulkAddPersonRunImportUiState

}

val BulkAddPersonRunImportComponent = FC<BulkAddPersonRunImportProps> { props ->
    val muiAppState = useMuiAppState()
    val strings = useStringProvider()

    UstadStandardContainer {
        when {
            props.uiState.inProgress -> {
                Grid {
                    container = true
                    direction = responsive(GridDirection.column)

                    sx {
                        useCenterAlignGridContainer(muiAppState)
                    }

                    Grid {
                        item = true

                        LinearProgress {
                            sx {
                                width = 200.px
                            }
                            variant = if(props.uiState.totalRecords > 0) {
                                value = props.uiState.progress * 100
                                LinearProgressVariant.determinate
                            }else {
                                LinearProgressVariant.indeterminate
                            }
                        }
                    }

                    Grid {
                        item = true
                        + strings[MR.strings.importing]
                    }
                }
            }

            props.uiState.hasErrors -> {
                List {
                    props.uiState.errorMessage?.also { errorMessage ->
                        ListItem {
                            ListItemIcon {
                                ErrorOutlineIcon()
                            }

                            ListItemText {
                                primary = ReactNode(errorMessage)
                            }
                        }
                    }

                    props.uiState.errors.forEach { error ->
                        ListItem {
                            ListItemIcon {
                                ErrorOutlineIcon()
                            }

                            ListItemText {
                                primary = ReactNode(
                                    "${strings[MR.strings.error]} ${strings[MR.strings.line_number]} " +
                                            "${error.lineNum} ${error.colName} - ${error.invalidValue}"
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                Grid {
                    container = true
                    direction = responsive(GridDirection.column)

                    sx {
                        useCenterAlignGridContainer(muiAppState)
                    }

                    Grid {
                        item = true

                        CheckIcon()
                    }

                    Grid {
                        item = true
                        + "${strings[MR.strings.imported]}: ${props.uiState.numImported}"
                    }
                }
            }
        }
    }
}

val BulkAddPersonRunImportScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        BulkAddPersonRunImportViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(
        BulkAddPersonRunImportUiState())

    BulkAddPersonRunImportComponent {
        uiState = uiStateVal
    }
}
