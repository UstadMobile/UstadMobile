package com.ustadmobile.port.android.view

import android.app.ProgressDialog
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadViewWithProgressDialog


open class UstadBaseWithContentOptionsActivity : UstadBaseActivity(),UstadViewWithProgressDialog {


    internal var coordinatorLayout: CoordinatorLayout? = null


    internal lateinit  var importDialog: ProgressDialog

    fun showBaseMessage(message: String) {
        Snackbar.make(coordinatorLayout!!, message, Snackbar.LENGTH_LONG).show()
    }

    override fun showProgressDialog(show: Boolean) {
        if(show){
            importDialog.show()
        }else{
            importDialog.dismiss()
        }
    }

}
