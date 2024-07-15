package com.ustadmobile.core.domain.xxhash


/**
 * XXHash is used to generate a 64bit unique ID for situations where:
 *   1) The number of unique IDs is in the thousands (not millions) e.g. xAPI verb and activity IDs
 *   2) We don't want to maintain a mapping between a String URL and a 64bit/32bit numerical ID.
 *
 * Using a hash of the string ensures that when data is synced relations will 'just work' e.g.
 * if two nodes both record a statement offline, where neither node has seen the verb being used
 * before, both will generate exactly the same primary key. Conflict resolution will work as expected,
 * e.g. when records sync with the server, both statement entities will still link to the same verb
 * entity.
 */
interface XXStringHasher {

    fun hash(string: String): Long

}