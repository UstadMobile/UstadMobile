package com.ustadmobile.test.core.impl

import android.app.Activity
import android.os.Handler
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingResource
import com.ustadmobile.port.android.view.UstadBaseFragment
import com.ustadmobile.port.android.view.UstadDetailFragment
import com.ustadmobile.port.android.view.UstadEditFragment
import com.ustadmobile.test.rules.ScenarioIdlingResource
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Note: There is a lot of overlap between this and DataBindingIdlingResource. DataBindingIdlingResource
 * closely tracks example repositories from Google. That is why there is no common parent class
 * between the two.
 */
class CrudIdlingResource : IdlingResource, ScenarioIdlingResource {
    // list of registered callbacks
    private val idlingCallbacks = mutableListOf<IdlingResource.ResourceCallback>()

    // give it a unique id to workaround an espresso bug where you cannot register/unregister
    // an idling resource w/ the same name.
    private val id = UUID.randomUUID().toString()

    // holds whether isIdle is called and the result was false. We track this to avoid calling
    // onTransitionToIdle callbacks if Espresso never thought we were idle in the first place.
    private var wasNotIdle = false

    private var fragmentScenario: FragmentScenario<out Fragment>? = null

    private var activityScenario: ActivityScenario<out Activity>? = null

    /**
     * View IDs that will not be considered when checking for idle progress bars and recyclerviews
     */
    val excludedViewIds : MutableList<Int> = CopyOnWriteArrayList()


    override fun getName() = "DataBinding $id"

    /**
     * Sets the fragment from a [FragmentScenario] to be used from [DataBindingIdlingResource].
     */
    override fun monitorFragment(fragmentScenario: FragmentScenario<out Fragment>?) {
        this.fragmentScenario = fragmentScenario
    }

    override fun monitorActivity(activityScenario: ActivityScenario<out Activity>?) {
        this.activityScenario = activityScenario
    }

    override fun isIdleNow(): Boolean {
        val idle = getFragments().all { it.isIdle() }

        if (idle) {
            if (wasNotIdle) {
                // notify observers to avoid espresso race detector
                idlingCallbacks.forEach { it.onTransitionToIdle() }
            }
            wasNotIdle = false
        } else {
            wasNotIdle = true
            val fragmentScenarioVal = fragmentScenario
            val activityScenarioVal = activityScenario

            if(fragmentScenarioVal != null) {
                fragmentScenarioVal.onFragment { fragment ->
                    fragment.view?.postDelayed({
                        if (fragment.view != null) {
                            isIdleNow
                        }
                    }, 16)
                }
            }else if(activityScenarioVal != null) {
                activityScenarioVal.onActivity {
                    Handler().postDelayed({
                        isIdleNow
                    }, 16)
                }
            }else {
                throw IllegalStateException("CrudIdlingResource is not connected to any " +
                        "fragment or activity scenario! If you are using a test @Rule, please " +
                        "make sure to call monitorFragment or monitorActivity **BEFORE** any Espresso" +
                        "onView or onIdle call")
            }
        }

        return idle
    }

    fun getFragments(): List<Fragment> {
        val fragments = mutableListOf<Fragment>()
        fragmentScenario?.onFragment {
            fragments += it.flattenHierachy()
        }

        activityScenario?.onActivity {
            val activityFragments = (it as? FragmentActivity)?.supportFragmentManager
                    ?.fragments ?: listOf()
            activityFragments += activityFragments.flatMap { it.flattenHierachy() }
        }

        return fragments
    }

    private fun Fragment.flattenHierachy(): List<Fragment> {
        return listOf(this) + childFragmentManager.fragments.mapNotNull { it.flattenHierachy() }.flatten()
    }

    fun Fragment.isIdle(): Boolean {
        //if the fragment view is not currently live, it doesn't matter
        if(!this.viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
            return true

        if(this is UstadBaseFragment && (this.loading)){
           return false
        }

        if(this is UstadEditFragment<*> && (this.entity == null || !this.fieldsEnabled)) {
            return false
        }

        if(this is UstadDetailFragment<*> && (this.entity == null)) {
            return false
        }

        //look for recyclerviews or progressbar
        if(this.view?.flattenHierarchy()?.filter { it.id !in excludedViewIds }?.any {
                    (it as? RecyclerView)?.isIdle() ?: (it as? ProgressBar)?.isIdle() == false
                } == true) {
            return false
        }

        return true
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        idlingCallbacks.add(callback)
    }

    private fun ProgressBar.isIdle(): Boolean {
        return visibility == VISIBLE && (isIndeterminate || progress < max)
    }

    private fun RecyclerView.isIdle() : Boolean{
        if(hasPendingAdapterUpdates())
            return false

        val adapterVal = adapter ?: return false

        val adapters = if(adapterVal is MergeAdapter) adapterVal.adapters else listOf(adapterVal)
        return adapters.all { it.isIdle() }
    }

    private fun RecyclerView.Adapter<*>.isIdle() : Boolean {
        if(this is PagedListAdapter<*, *> && this.currentList == null) {
            return false
        }

        return true
    }

    private fun View.flattenHierarchy(): List<View> = if (this is ViewGroup) {
        listOf(this) + children.map { it.flattenHierarchy() }.flatten()
    } else {
        listOf(this)
    }
}