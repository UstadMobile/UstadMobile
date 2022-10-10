package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class ConnectivityStatus() {

    @PrimaryKey
    var csUid = 1

    var connectivityState: Int = 0

    var wifiSsid: String? = null

    var connectedOrConnecting: Boolean = false

    constructor(connectivityState: Int, connectedOrConnecting: Boolean, wifiSsid: String?) : this() {
        this.connectivityState = connectivityState
        this.connectedOrConnecting = connectedOrConnecting
        this.wifiSsid = wifiSsid
    }

    override fun toString(): String {
        var `val` = ""
        when (connectivityState) {
            STATE_METERED -> `val` += "METERED"
            STATE_UNMETERED -> `val` += "UNMETERED"
            STATE_DISCONNECTED -> `val` += "DISCONNECTED"
            STATE_CONNECTED_LOCAL -> `val` += "CONNECTED_LOCAL"
            STATE_CONNECTING_LOCAL -> `val` += "CONNECTING_LOCAL"
        }

        if (wifiSsid != null) {
            `val` += " SSID = \"$wifiSsid\""
        }

        `val` += " connectedOrConnecting = $connectedOrConnecting"

        return `val`
    }

    companion object {

        const val STATE_DISCONNECTED = 0

        const val STATE_CONNECTING_LOCAL = 1

        const val STATE_CONNECTED_LOCAL = 2

        const val STATE_METERED = 3

        const val STATE_UNMETERED = 4
    }
}
