package com.ustadmobile.core.viewmodel.clazz.gradebook

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultError
import app.cash.paging.PagingSourceLoadResultInvalid
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.paging.PagingSourceWithHttpLoader
import com.ustadmobile.lib.db.composites.PersonAndClazzMemberListDetails
import com.ustadmobile.lib.db.composites.StudentAndBlockStatuses

/**
 * The StudentProgress report is a little different to most queries because we have a list of
 * students, and for each students, 0 .. n courseblocks for which we need to show a result.
 *
 * How this (will) work:
 *  a) the studentListPagingSource will be the paging source from repository.
 *  b) The load function will use studentListPagingSource to load the list of students, and then
 *     use the params to run another query to get the BlockResult for each student in the list
 *  c) The repository function will download (using http load functions) the StatementEntity(s) that
 *     that are required. This PagingSource will implement the same interface as
 *     DoorRepositoryReplicatePullPagingSource so that it can be used by DoorRemoteMediator the same
 *     way.
 */
class ClazzProgressReportPagingSource(
    private val studentListPagingSource: PagingSource<Int, PersonAndClazzMemberListDetails>,
    private val db: UmAppDatabase,
): PagingSource<Int, StudentAndBlockStatuses>(), PagingSourceWithHttpLoader<Int> {

    override fun getRefreshKey(state: PagingState<Int, StudentAndBlockStatuses>): Int? {
        return studentListPagingSource.getRefreshKey(
            PagingState(
                pages = state.pages.map {
                    PagingSourceLoadResultPage(
                        data = it.data.map { it.student },
                        prevKey = it.prevKey,
                        nextKey = it.nextKey
                    )
                },
                anchorPosition = state.anchorPosition,
                config = state.config,
                leadingPlaceholderCount = 0
            )
        )
    }

    override suspend fun load(
        params: PagingSourceLoadParams<Int>
    ): PagingSourceLoadResult<Int, StudentAndBlockStatuses> {
        //Will need to convert params into offset and limit to do query to find statements OR use an IN syntax

        val studentListResult = studentListPagingSource.load(params)
        return when(studentListResult) {
            is PagingSourceLoadResultPage<*, *> -> {
                val studentListResultCasted = studentListResult as
                        PagingSourceLoadResultPage<Int, PersonAndClazzMemberListDetails>
                PagingSourceLoadResultPage<Int, StudentAndBlockStatuses>(
                    data = studentListResultCasted.data.map {
                        StudentAndBlockStatuses(
                            student = it,
                            blockStatuses = emptyList()
                        )
                    },
                    prevKey = studentListResultCasted.prevKey,
                    nextKey = studentListResultCasted.nextKey,
                    itemsBefore = studentListResultCasted.itemsBefore,
                    itemsAfter = studentListResultCasted.itemsAfter
                )
            }
            is PagingSourceLoadResultError<*, *> -> {
                PagingSourceLoadResultError<Int, StudentAndBlockStatuses>(studentListResult.throwable)
            }
            is PagingSourceLoadResultInvalid<*, *> -> {
                PagingSourceLoadResultInvalid<Int, StudentAndBlockStatuses>()
            }
            else -> {
                throw IllegalStateException("Cant get here really, but compiler doesn't know that")
            }
        } as PagingSourceLoadResult<Int, StudentAndBlockStatuses>
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun loadHttp(params: PagingSourceLoadParams<Int>): Boolean {
        return (studentListPagingSource as? PagingSourceWithHttpLoader<Int>)?.loadHttp(params) ?: true
    }
}