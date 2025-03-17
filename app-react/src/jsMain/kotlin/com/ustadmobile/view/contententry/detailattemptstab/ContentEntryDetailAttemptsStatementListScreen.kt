package com.ustadmobile.view.contententry.detailattemptstab

import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.util.ext.displayName
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.xapi.StatementEntityAndVerb
import com.ustadmobile.lib.db.entities.xapi.VerbEntity
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.mui.components.UstadNothingHereYet
import com.ustadmobile.util.ext.isSettledEmpty
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import mui.icons.material.Check as CheckIcon
import mui.material.Box
import mui.material.Chip
import mui.material.ChipVariant
import mui.material.Container
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useRequiredContext
import tanstack.react.query.UseInfiniteQueryResult
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct

private const val LOAD_SIZE = 50
private const val WIDTH = 100

external interface ContentEntryDetailAttemptsStatementListProps : Props {
    var uiState: ContentEntryDetailAttemptsStatementListUiState
    var refreshCommandFlow: Flow<RefreshCommand>?
    var onSortOrderChanged: (SortOrderOption) -> Unit
    var onVerbFilterToggled: (VerbEntity) -> Unit
}

val ContentEntryDetailAttemptsStatementListScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryDetailAttemptsStatementListViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(ContentEntryDetailAttemptsStatementListUiState())

    ContentEntryDetailAttemptsStatementListComponent {
        this.uiState = uiState
        refreshCommandFlow = viewModel.refreshCommandFlow
        onSortOrderChanged = viewModel::onSortOrderChanged
        onVerbFilterToggled = viewModel::onVerbFilterToggled
    }
}

val ContentEntryDetailAttemptsStatementListComponent = FC<ContentEntryDetailAttemptsStatementListProps> { props ->
    val theme by useRequiredContext(ThemeContext)

    val remoteMediatorResult = useDoorRemoteMediator(
        pagingSourceFactory = props.uiState.attemptsStatementList,
        refreshCommandFlow = (props.refreshCommandFlow ?: emptyFlow())
    )

    val infiniteQueryResult: UseInfiniteQueryResult<PagingSourceLoadResult<Int, StatementEntityAndVerb>, Throwable> =
        usePagingSource(remoteMediatorResult.pagingSourceFactory, true, LOAD_SIZE)

    val muiAppState = useMuiAppState()
    val isSettledEmpty = infiniteQueryResult.isSettledEmpty(remoteMediatorResult)

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = WIDTH.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            item("sort_list_opts") {
                UstadListSortHeader.create {
                    activeSortOrderOption = props.uiState.sortOption
                    sortOptions = props.uiState.sortOptions
                    enabled = true
                    onClickSort = props.onSortOrderChanged
                }
            }

            item("verb_filters") {
                Box.create {
                    id = "verb_filters"

                    sx {
                        padding = theme.spacing(1)
                        overflowX = Overflow.scroll
                    }

                    props.uiState.availableVerbs.forEach { verb ->
                        val isSelected = verb.verbEntity.verbUid !in props.uiState.deselectedVerbUids
                        Chip {
                            sx {
                                marginLeft = theme.spacing(1)
                                marginRight = theme.spacing(1)
                            }
                            onClick = {
                                props.onVerbFilterToggled(verb.verbEntity)
                            }

                            icon = if (isSelected) {
                                CheckIcon.create()
                            }else {
                                null
                            }
                            label = ReactNode(verb.displayName().capitalizeFirstLetter())
                            variant = if(isSelected) {
                                ChipVariant.filled
                            }else {
                                ChipVariant.outlined
                            }
                        }
                    }
                }
            }

            if (isSettledEmpty) {
                item("empty_state") {
                    UstadNothingHereYet.create()
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { "${it.statementEntity.statementIdHi}-${it.statementEntity.statementIdLo}" }
            ) { item ->
                StatementEntityAndVerbListItem.create {
                    statement = item
                }
            }
        }

        Container {
            VirtualListOutlet()
        }
    }
}