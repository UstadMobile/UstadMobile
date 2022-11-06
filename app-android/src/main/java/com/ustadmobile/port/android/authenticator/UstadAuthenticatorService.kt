package com.ustadmobile.port.android.authenticator

import android.app.Service
import android.content.Intent
import android.os.IBinder

class UstadAuthenticatorService: Service() {

    override fun onBind(intent: Intent?): IBinder {
        return UstadAccountAuthenticator(this).iBinder
    }
}