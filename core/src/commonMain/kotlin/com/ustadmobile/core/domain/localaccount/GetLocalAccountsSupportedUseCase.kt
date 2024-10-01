package com.ustadmobile.core.domain.localaccount

class GetLocalAccountsSupportedUseCase(private val localAccountsSupported: Boolean) {

    operator fun invoke(): Boolean = localAccountsSupported

}