package com.ustadmobile.core.util.ext

/**
 * Extension functions that handle adding/removing/appending subtree paths in a Uri.
 *
 * When a user selects a folder using ACTION_OPEN_DOCUMENT_TREE, we must use DocumentFile.fromTreeUri
 * to obtain a DocumentFile object which can then be used to list contents. This works ONLY for the
 * Uri originally selected by the user. If there are subfolders, then the DocumentFile.uri
 * returned for those subfolders cannot be used to lookup a DocumentFile using
 * DocumentFile.fromTreeUri, making it impossible to list the files in the subfolder even though we
 * have the subfolder's Uri.
 *
 * The workaround approach is to create a new Uri which consists of the original tree Uri and the
 * subpath appended in the Uri fragment e.g. content://some/path/selected#subfolder1%2Fsubfolder2 .
 * It is possible to use this to lookup a DocumentFile by using DocumentFile.fromTreeUri and then
 * following the subpath via DocumentFile.listFiles .
 */

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile

private const val SUBTREE_FRAGMENT_PREFIX = "_ustadSubTreePath="

/**
 * Add a subtree path to the fragment of thhe given Uri.
 *
 * @param subTreePath the subtree path to add to the Uri fragment
 *
 * @return a new Uri where the subtree path is added to the fragment
 */
fun Uri.withSubTreePathFragment(subTreePath: String): Uri {
    return this.buildUpon()
        .fragment("${this.fragment ?: ""}$SUBTREE_FRAGMENT_PREFIX$subTreePath")
        .build()
}

/**
 * Extract the original tree Uri and the subtree path (if any) from the receiver Uri
 *
 * @return a Pair containing the original tree Uri (if a subtree path was present, otherwise, the
 * original uri) and the subtree path (if any, otherwise null)
 */
fun Uri.extractSubTreePath(): Pair<Uri, String?> {
    val fragmentVal = fragment
    val path = fragmentVal?.substringAfter(SUBTREE_FRAGMENT_PREFIX, "")
        ?.takeIf { it.isNotEmpty() }

    return buildUpon().apply {
        when {
            path != null -> {
                val fragmentBeforePath = fragmentVal.substringBefore(SUBTREE_FRAGMENT_PREFIX)

                if(fragmentBeforePath.isNotEmpty()) {
                    fragment(fragmentBeforePath)
                }else {
                    fragment(null)
                }
            }

            fragmentVal != null -> {
                fragment(fragmentVal)
            }
        }

    }.build() to path
}

/**
 * Convert the given receiver Uri to a DocumentFile by opening the original tree Uri and then
 * following the subpath using DocumentFile.listFiles function.
 */
fun Uri.toDocumentFileIncludingSubpath(
    context: Context,
): DocumentFile {
    val (baseUri, subTreePath) = extractSubTreePath()

    /*
     * Normally the base will be a tree Uri. Testing may use a file Uri.
     */
    var documentFile = if(baseUri.scheme == "file") {
        DocumentFile.fromFile(baseUri.toFile())
    }else {
        DocumentFile.fromTreeUri(context, baseUri)
    } ?: throw IllegalArgumentException("$baseUri is not a tree uri")

    subTreePath?.split("/")?.filter { it.isNotEmpty() }?.forEach {  subPathComponent ->
        documentFile = documentFile.listFiles().first { it.name == subPathComponent }
    }

    return documentFile
}

/**
 * Append a subfolder to the subtree path
 *
 * @return a Uri with the additional subfolder appended to the subtree path
 */
fun Uri.appendSubTreePath(subfolder: String): Uri {
    val (treeUri, subPath) = extractSubTreePath()
    return treeUri.withSubTreePathFragment("${subPath ?: ""}/$subfolder")
}
