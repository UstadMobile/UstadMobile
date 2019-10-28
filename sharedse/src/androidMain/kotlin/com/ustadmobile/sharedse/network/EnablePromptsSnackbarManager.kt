package com.ustadmobile.sharedse.network

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import java.util.*

/**
 * Used to manage a Snackbar that will prompt the user (once per session) to enable bluetooth
 * and wifi so that they can use peer to peer sharing.
 *
 * The snackbar will show a message with an action. The action will start an intent that will prompt
 * the user to enable the hardware required.
 */
class EnablePromptsSnackbarManager() {

    private val promptRequired = mutableMapOf(BLUETOOTH to false,
            WIFI to false)

    private val promptsShown = mutableMapOf(BLUETOOTH to false,
            WIFI to false)

    private val promptActions = mapOf(BLUETOOTH to BluetoothAdapter.ACTION_REQUEST_ENABLE,
            WIFI to Settings.ACTION_WIRELESS_SETTINGS)

    private val snackbarsShown = WeakHashMap<Int, Snackbar>()


    /**
     * Sets whether or not a prompt is required for a specific service. This is true when the device
     * has the needed hardware (e.g. Bluetooh / WiFi) but that hardware has been switched off.
     *
     * @param promptType constant BLUETOOTH or WIFI
     * @param promptRequired true if a prompt should be shown, false otherwise
     */
    fun setPromptRequired(promptType: Int, promptRequired: Boolean) {
        this.promptRequired[promptType] = promptRequired

        if(promptRequired) {
            val snackbarDisplayed = snackbarsShown[promptType]
            snackbarDisplayed?.dismiss()
            promptsShown[promptType] = false
        }
    }

    /**
     * Make a snackbar if a prompt when:
     *  1. A prompt is required for the given service (wifi or bluetooth)
     *  2. That prompt has not yet been shown
     *  3. There are no other snackbars currently visible
     *
     *  @param context Context
     *  @param snackbarMaker Function that will make a Snackbar (e.g. setting the root view)
     *  @param promptStringIds String IDs for the message to show the user to enable services. This
     *  has to be passed because strings are in the app-android module and this is in the sharedse
     *  module
     *  @param enableStringId String ID for the action button (Enable)
     */
    fun makeSnackbarIfRequired(context: Context,
                               snackbarMaker: (textId: Int) -> Snackbar,
                               promptStringIds: Map<Int, Int>,
                               enableStringId: Int): Snackbar? {
        var snackbar: Snackbar? = null
        if(snackbarsShown.any { it.value.isShown })
            return null

        for(promptType in listOf(BLUETOOTH, WIFI)) {
            if(promptRequired[promptType]!! && !promptsShown[promptType]!!) {
                promptRequired[promptType] = false
                snackbar = snackbarMaker(promptStringIds[promptType]!!)
                        .setAction(enableStringId, {
                            context.startActivity(Intent(promptActions[promptType]))
                        })
                snackbarsShown[promptType] = snackbar
                promptsShown[promptType] = true
                snackbar.show()
                break
            }
        }

        return snackbar
    }

    companion object {
        const val BLUETOOTH = 1

        const val WIFI = 2
    }

}