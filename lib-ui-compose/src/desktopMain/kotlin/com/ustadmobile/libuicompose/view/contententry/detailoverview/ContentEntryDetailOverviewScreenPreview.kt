package com.ustadmobile.libuicompose.view.contententry.detailoverview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewUiState
import com.ustadmobile.lib.db.composites.ContentEntryAndDetail
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryButtonModel
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import com.ustadmobile.libuicompose.view.contententry.detailoverviewtab.ContentEntryDetailOverviewScreen


@Composable
@Preview
fun ContentEntryDetailOverviewScreenPreview() {
    val uiStateVal = ContentEntryDetailOverviewUiState(
        contentEntry = ContentEntryAndDetail(
            entry = ContentEntry().apply {
                title = "Content Title"
                author = "Author"
                publisher = "Publisher"
                licenseName = "BY_SA"
                description = "Content Description"
            }
        ),
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
        locallyAvailable = true,
        markCompleteVisible = true,
        translationVisibile = true
    )

    ContentEntryDetailOverviewScreen(
        uiState = uiStateVal
    )

}