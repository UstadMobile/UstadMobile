package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5

/**
 * Given a list of ContainerEntryWithMd5(s) (e.g. a list of ContainerEntry(s) that may need downloaded
 * or uploaded), get a list of the distinct md5s to transfer. The MD5 list must be sorted, so that
 * if the transfer is interrupted, the transfer can resume from the same entry.
 */
fun List<ContainerEntryWithMd5>.distinctMds5sSorted() : List<String> {
    return mapNotNull { it.cefMd5 }.distinct().sorted()
}

/**
 * Given a list of ContainerEntryWithMd5(s) (e.g. a list of ContainerEntry(s) that may need downloaded
 * or uploaded), get a string of the distinct md5s (e.g. as per distinctMds5sSorted) in hex form
 * separated by ; . This hex deliminated list is used to avoid http encoding and filename issues.
 */
fun List<ContainerEntryWithMd5>.distinctMd5sSortedAsJoinedQueryParam(): String {
    return distinctMds5sSorted().map { it.base64EncodedToHexString() }.joinToString(separator = ";")
}
