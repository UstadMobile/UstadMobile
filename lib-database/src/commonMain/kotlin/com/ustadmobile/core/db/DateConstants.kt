package com.ustadmobile.core.db


/**
 * Unix timestamp for maximum date we will recognize as being a set date: 1/Jan/2200
 */
const val MAX_VALID_DATE = 7258118400000

const val MS_PER_HOUR = (60 * 60 * 1000)

/**
 * Unix timestamp 24hours beyond the maximum date that we will recognize. We must not use Long.MAX_VALUE,
 * because adding anything (e.g. timezone offset) to MAX_VALUE will wrap to the most negative possible
 * value, leading to unpredictable behavior.
 */
const val UNSET_DISTANT_FUTURE = MAX_VALID_DATE + (24 * MS_PER_HOUR)
