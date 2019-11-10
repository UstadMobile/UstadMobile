package com.ustadmobile.port.android.view

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.Fragment
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.LoginView
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.android.impl.LastActive
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by mike on 10/15/15.
 */
open class UstadBaseFragment : Fragment() {

    private val runOnAttach = Vector<Runnable>()

    var icon: Int?=null
    var title: Int?=null

    var checkLogout:Boolean = true

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * UstadBaseActivity overrides the onBackPressed and will ask all visible fragments if they want
     * to override the back button press.  This could be used to handle a back button press
     * on an internal browser or to close a menu etc.
     *
     * @return true if the fragment can go back and wants to addAuthHeader the back button press, false otherwise
     */
    fun canGoBack(): Boolean {
        return false
    }

    /**
     * UstadBaseActivity will call this method if canGoBack returned true.  This can be used to
     * go back in an internal webview or close a menu for example.
     */
    fun goBack() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val systemTime = AtomicLong(UMCalendarUtil.getDateInMilliPlusDays(0))
        checkTimeoutAndUpdateLastActive(systemTime)
    }

    fun checkTimeoutAndUpdateLastActive(time:AtomicLong):Boolean{
        if(!checkTimeout()){
            updateLastActive(time)
            return true
        }
        return false
    }

    fun updateLastActive(time: AtomicLong){
        LastActive.instance.lastActive = time
        val impl = UstadMobileSystemImpl.instance
        impl.setAppPref(UstadBaseActivity.PREFKEY_LAST_ACTIVE, time.toString(),
                context!!)
    }

    private fun checkTimeout(): Boolean {
        val impl = UstadMobileSystemImpl.instance
        val lastInputEventTime = LastActive.instance.lastActive
        var lt:Long
        var lastActiveString = impl.getAppPref(UstadBaseActivity.PREFKEY_LAST_ACTIVE, context!!)
        if (lastActiveString != null && !lastActiveString.isEmpty())
        {
            lt = java.lang.Long.parseLong(lastActiveString)
        }
        else
        {
            lt = 0
        }
        val timeoutExceeded = System.currentTimeMillis() - lt
        val logoutTimeout = UstadBaseActivity.TIMEOUT_LOGOUT
        //TODO: Get and set from app pref
        if (timeoutExceeded > logoutTimeout)
        {
            handleLogout()
            return false
        }else{
            return true
        }
    }

    private fun handleLogout() {
        if (checkLogout)
        {
            var currentUsername:String ?= null
            if (UmAccountManager.getActiveAccount(context!!) != null)
            {
                currentUsername = UmAccountManager.getActiveAccount(context!!)!!.username
            }
            finishAffinity(activity!!)
            val blankAccount = UmAccount(0, null, null,null)
            UmAccountManager.setActiveAccount(blankAccount, context!!)
            val impl = UstadMobileSystemImpl.instance
            val args = HashMap<String, String>()
            if (currentUsername != null)
            {
                args.put(Login2View.ARG_LOGIN_USERNAME, currentUsername)
            }
            impl.go(LoginView.VIEW_NAME, args, context!!)
        }
    }
    fun runOnUiThread(r: Runnable?) {
        if (activity != null) {
            activity!!.runOnUiThread(r)
        } else {
            runOnAttach.add(r)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        val runnables = runOnAttach.iterator()
        while (runnables.hasNext()) {
            val current = runnables.next()
            current.run()
            runnables.remove()
        }
    }
}
