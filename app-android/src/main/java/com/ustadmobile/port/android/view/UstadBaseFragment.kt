package com.ustadmobile.port.android.view

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ustadmobile.port.android.view.util.FabManagerLifecycleObserver
import com.ustadmobile.port.android.view.util.TitleLifecycleObserver
import java.util.*

/**
 * Created by mike on 10/15/15.
 */
open class UstadBaseFragment : Fragment() {

    private val runOnAttach = Vector<Runnable>()

    protected var titleLifecycleObserver: TitleLifecycleObserver? = null

    protected var fabManager: FabManagerLifecycleObserver? = null

    /**
     * If enabled, the fab will be managed by this fragment when its view is active.
     */
    protected var fabManagementEnabled: Boolean = true

    var title: String?
        get() = titleLifecycleObserver?.title
        set(value) {
            titleLifecycleObserver?.title = value
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleLifecycleObserver = TitleLifecycleObserver(null, (activity as? AppCompatActivity)?.supportActionBar).also {
            viewLifecycleOwner.lifecycle.addObserver(it)
        }

        if(fabManagementEnabled) {
            fabManager = FabManagerLifecycleObserver(
                    (activity as? UstadListViewActivityWithFab)?.activityFloatingActionButton,
                false, 0, null).also {
                viewLifecycleOwner.lifecycle.addObserver(it)
            }
        }
    }

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

    companion object {

        /**
         * Argument to pass to tell a fragment where on the back stack a result (e.g. entity selected
         * from a list or newly created) should be saved. This works along the principles outlined
         * here: https://developer.android.com/guide/navigation/navigation-programmatic#returning_a_result .
         *
         * The difference between the approach taken here and the approach in the link above is that
         * we do not automatically save the result to the previous entry in the back stack. When the
         * user goes from fragment a to a list to pick an entity, and then selects to create a new
         * entity, we want to go back directly back from the new entity edit fragment to fragment a
         * (e.g. skip the intermediary list).
         *
         * @see com.ustadmobile.port.android.view.ext.FragmentExtKt#saveResultToBackStackSavedStateHandle
         */
        const val ARG_RESULT_DEST_ID = "result_dest"

        /**
         * The key to use in the SavedStateHandle to save the result
         */
        const val ARG_RESULT_DEST_KEY = "result_key"

    }

}
