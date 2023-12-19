package com.ustadmobile.port.android.view

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.ustadmobile.core.view.UstadView
import dev.icerock.moko.resources.StringResource
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI

/**
 * Base activity to handle interacting with UstadMobileSystemImpl
 *
 *
 * Created by mike on 10/15/15.
 */
abstract class UstadBaseActivity : AppCompatActivity(), UstadView,
    DIAware
{

    override val di by closestDI()


    override fun onCreate(savedInstanceState: Bundle?) {
        WebView.setWebContentsDebuggingEnabled(true)

        super.onCreate(savedInstanceState)

    }


    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: StringResource?) {

    }

}