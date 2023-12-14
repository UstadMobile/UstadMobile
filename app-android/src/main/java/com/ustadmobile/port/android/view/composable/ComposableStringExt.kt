package com.ustadmobile.port.android.view.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ustadmobile.core.R as CR

/**
 * Shorthand to add (optional) to a field title e.g.
 * "FieldName" -> "Field name (optional)"
 */
@Composable
fun String.addOptionalSuffix() = "$this (${stringResource(id = CR.string.optional)})"

