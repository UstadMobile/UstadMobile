package com.ustadmobile.core.account

/**
 * Represents parameters for Pbkdf2 encryption. In production, this would normally be set in the
 * application.conf file.
 *
 * This object can then be bound into the DI for retrieval as needed.
 */
data class Pbkdf2Params (val iterations: Int = 10000, val keyLength: Int = 512)