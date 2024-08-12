package com.ustadmobile.view.contententry.getmetadata

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetadataStatus
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataUiState
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.util.ext.useCenterAlignGridContainer
import mui.material.CircularProgress
import mui.material.CircularProgressVariant
import mui.material.Grid
import mui.material.GridDirection
import mui.material.SvgIconSize
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import web.cssom.PaddingLeft
import mui.icons.material.ErrorOutline as ErrorIcon

external interface ContentEntryGetMetadataProps: Props {
    var uiState: ContentEntryGetMetadataUiState
}

val ContentEntryGetMetadataComponent = FC<ContentEntryGetMetadataProps> {props ->
    val muiAppState = useMuiAppState()
    val strings = useStringProvider()

    Grid {
        container = true
        direction = responsive(GridDirection.column)
        sx {
            useCenterAlignGridContainer(muiAppState)
        }

        val errorStr = props.uiState.status.error

        if(errorStr == null) {
            Grid {
                item = true

                CircularProgress {
                    sx {
                        paddingLeft = "auto".unsafeCast<PaddingLeft>()
                        paddingRight = "auto".unsafeCast<PaddingLeft>()
                    }
                    variant = if(props.uiState.status.indeterminate) {
                        CircularProgressVariant.indeterminate
                    }else {
                        CircularProgressVariant.determinate
                    }
                    if(!props.uiState.status.indeterminate) {
                        value = props.uiState.status.progress
                    }
                }
            }

            Grid {
                item = true

                + "${strings[MR.strings.uploading]}: "
                + "${UMFileUtil.formatFileSizeMb(props.uiState.status.processedBytes)} /"
                + "${UMFileUtil.formatFileSizeMb(props.uiState.status.totalBytes)} "
            }
        }else {
            Grid {
                item = true

                ErrorIcon {
                    fontSize = SvgIconSize.large
                }
            }

            Grid {
                item = true

                + errorStr
            }
        }

    }

}

val ContentEntryGetMetadataScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryGetMetadataViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ContentEntryGetMetadataUiState())

    ContentEntryGetMetadataComponent {
        uiState = uiStateVal
    }

}

val ContentEntryGetMetadataPreview = FC<Props> {
    ContentEntryGetMetadataComponent {
        uiState = ContentEntryGetMetadataUiState(
            status = ContentEntryGetMetadataStatus(
                indeterminate = true,
                error = "SNAFU"
            ),
        )
    }
}
