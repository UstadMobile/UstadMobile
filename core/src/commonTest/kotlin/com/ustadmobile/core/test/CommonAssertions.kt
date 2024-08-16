package com.ustadmobile.core.test

import kotlin.math.abs
import kotlin.test.assertTrue

fun assertEqualsWithinThreshold(
    expected: Long,
    actual: Long,
    threshold: Long,
    message: String? = null
) {
    assertTrue(isWithinThreshold(expected, actual, threshold), message)
}

fun isWithinThreshold(expected: Long, actual: Long, threshold: Long): Boolean {
    return abs(expected - actual) < threshold
}
