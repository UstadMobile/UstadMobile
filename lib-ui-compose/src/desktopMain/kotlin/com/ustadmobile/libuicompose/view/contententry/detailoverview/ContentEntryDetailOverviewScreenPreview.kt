package com.ustadmobile.libuicompose.view.contententry.detailoverview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewUiState
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryButtonModel
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer
import com.ustadmobile.lib.db.entities.ContentJobItemProgress
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.StatementEntity
import com.ustadmobile.libuicompose.view.contententry.detail.ContentEntryDetailOverviewScreen


@Composable
@Preview
fun ContentEntryDetailOverviewScreenPreview() {
    val uiStateVal = ContentEntryDetailOverviewUiState(
        contentEntry = ContentEntryWithMostRecentContainer().apply {
            title = "Content Title"
            author = "Author"
            publisher = "Publisher"
            licenseName = "BY_SA"
            container = Container().apply {
                fileSize = 50
            }
            description = "Content Description"
        },
        scoreProgress = ContentEntryStatementScoreProgress().apply {
            /*@FloatRange(from = 0.0, to = 1.0)*/
            progress = 4

            success = StatementEntity.RESULT_FAILURE
            resultScore = 4
            resultMax = 40
        },
        contentEntryButtons = ContentEntryButtonModel().apply {
            showDownloadButton = true
            showOpenButton = true
            showDeleteButton = true
            showManageDownloadButton = true
        },
        availableTranslations = listOf(
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 0
                    name = "Persian"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 1
                    name = "English"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 2
                    name = "Korean"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 3
                    name = "Tamil"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 4
                    name = "Turkish"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 5
                    name = "Telugu"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 6
                    name = "Marathi"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 7
                    name = "Vietnamese"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 8
                    name = "Japanese"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 9
                    name = "Russian"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 10
                    name = "Portuguese"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 11
                    name = "Bengali"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 12
                    name = "Spanish"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 13
                    name = "Hindi"
                }
            }
        ),
        activeContentJobItems = listOf(
            ContentJobItemProgress().apply {
                cjiUid = 0
                progressTitle = "First"
                progress = 30
            },
            ContentJobItemProgress().apply {
                cjiUid = 1
                progressTitle = "Second"
                progress = 10
            },
            ContentJobItemProgress().apply {
                cjiUid = 2
                progressTitle = "Third"
                progress = 70
            }
        ),
        locallyAvailable = true,
        markCompleteVisible = true,
        translationVisibile = true
    )

    ContentEntryDetailOverviewScreen(
        uiState = uiStateVal
    )

}