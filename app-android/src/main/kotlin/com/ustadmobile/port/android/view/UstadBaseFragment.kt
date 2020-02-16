package com.ustadmobile.port.android.view

import android.content.Context
import androidx.fragment.app.Fragment
import java.util.*

/**
 * Created by mike on 10/15/15.
 */
open class UstadBaseFragment : Fragment() {

    private val runOnAttach = Vector<Runnable>()

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

    fun runOnUiThread(r: Runnable?) {
        if (activity != null) {
            activity!!.runOnUiThread(r)
        } else {
            runOnAttach.add(r)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val runnables = runOnAttach.iterator()
        while (runnables.hasNext()) {
            val current = runnables.next()
            current.run()
            runnables.remove()
        }
    }
}
