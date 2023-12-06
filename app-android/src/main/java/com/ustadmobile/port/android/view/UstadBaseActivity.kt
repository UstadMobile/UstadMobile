package com.ustadmobile.port.android.view

import android.content.Context
import android.hardware.SensorManager
import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.squareup.seismic.ShakeDetector
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.port.android.impl.UserFeedbackException
import dev.icerock.moko.resources.StringResource
import org.acra.ACRA
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import com.ustadmobile.core.R as CR

/**
 * Base activity to handle interacting with UstadMobileSystemImpl
 *
 *
 * Created by mike on 10/15/15.
 */
abstract class UstadBaseActivity : AppCompatActivity(), UstadView, ShakeDetector.Listener,
    DIAware
{

    override val di by closestDI()

    private var shakeDetector: ShakeDetector? = null
    private var sensorManager: SensorManager? = null
    internal var feedbackDialogVisible = false


    override fun onCreate(savedInstanceState: Bundle?) {
        WebView.setWebContentsDebuggingEnabled(true)

        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector(this)

    }

    override fun hearShake() {

        if (feedbackDialogVisible) {
            return
        }

        feedbackDialogVisible = true
        val builder = AlertDialog.Builder(this)
        builder.setTitle(CR.string.send_feedback)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.view_feedback_layout, null)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.feedback_edit_comment)
        builder.setView(dialogView)
        builder.setPositiveButton(CR.string.send) { dialogInterface, whichButton ->
            ACRA.errorReporter.handleSilentException(UserFeedbackException(editText.text.toString()))
            Toast.makeText(this, CR.string.feedback_thanks, Toast.LENGTH_LONG).show()
            dialogInterface.cancel()
        }
        builder.setNegativeButton(CR.string.cancel) { dialogInterface, i -> dialogInterface.cancel() }
        builder.setOnDismissListener { feedbackDialogVisible = false }
        builder.setOnCancelListener { feedbackDialogVisible = false }
        val dialog = builder.create()
        dialog.show()

    }

    override fun onResume() {
        super.onResume()

        if (shakeDetector != null && sensorManager != null) {
            shakeDetector?.start(sensorManager)
        }
    }

    override fun onPause() {
        super.onPause()
        shakeDetector?.stop()
    }


    public override fun onDestroy() {
        shakeDetector = null
        sensorManager = null
        super.onDestroy()
    }

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: StringResource?) {

    }

}