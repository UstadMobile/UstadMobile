package com.ustadmobile.appconfigdb.entities


data class SystemConfigAuth(
    var scaUid: Long = 0,
    var scaAuthType: Int = 0,
    var scaAuthId: String = "",
    var scaAuthCredential: String = "",
    var scaAuthSalt: String? = null,
) {

    companion object {

        const val TYPE_PASSWORD = 1

    }

}

