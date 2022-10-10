package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.ContainerEntryFile

/**
 * Whenever there is a possibility of a list parameter with more than 100 entries, this function
 * should be used so the maximum size of a list parameter is not exceeded (which would result in
 * a Room exception).
 */
fun ContainerEntryFileDao.findEntriesByMd5SumsSafe(md5Sums: List<String>, maxListParamSize: Int = 90) =
    findEntriesByMd5SumsSafeInternal(md5Sums, maxListParamSize, this::findEntriesByMd5Sums)

suspend fun ContainerEntryFileDao.findEntriesByMd5SumsSafeAsync(md5Sums: List<String>, maxListParamSize: Int) =
    findEntriesByMd5SumsSafeInternal(md5Sums, maxListParamSize) { findEntriesByMd5SumsAsync(it) }

suspend fun ContainerEntryFileDao.findExistingMd5SumsByMd5SumsSafe(
    md5Sums: List<String>,
    maxListParamSize: Int = 90
): List<String?> {
    return if(maxListParamSize > 0) {
        md5Sums.chunked(maxListParamSize).flatMap { findExistingMd5SumsByMd5SumsAsync(it) }
    }else {
        findExistingMd5SumsByMd5SumsAsync(md5Sums)
    }
}


internal inline fun findEntriesByMd5SumsSafeInternal(
    md5Sums: List<String>,
    maxListParamSize: Int,
    queryFn: (List<String>) -> List<ContainerEntryFile>
) : List<ContainerEntryFile>{
    return if (maxListParamSize > 0) {
        val chunkedList = md5Sums.chunked(maxListParamSize)
        val mutableList = mutableListOf<ContainerEntryFile>()
        chunkedList.forEach {
            queryFn(it).map { entryFile -> mutableList.add(entryFile) }
        }
        mutableList.toList()
    } else {
        queryFn(md5Sums)
    }
}