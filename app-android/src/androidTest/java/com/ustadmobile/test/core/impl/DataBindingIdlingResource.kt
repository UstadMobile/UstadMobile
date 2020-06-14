package com.ustadmobile.test.core.impl

import android.app.Activity
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingResource
import java.util.UUID

/**
 * An espresso idling resource implementation that reports idle status for all data binding
 * layouts. Data Binding uses a mechanism to post messages which Espresso doesn't track yet.
 *
 * Since this application runs UI tests at the fragment layer, this relies on implementations
 * calling [monitorFragment] with a [FragmentScenario], thereby monitoring all bindings in that
 * fragment and any child views.
 */
//Originally from:
// https://github.com/android/architecture-components-samples/blob/master/GithubBrowserSample/app/src/androidTest/java/com/android/example/github/util/DataBindingIdlingResource.kt

//See also (for activity) https://github.com/googlecodelabs/android-testing/blob/codelab2019/app/src/sharedTest/java/com/example/android/architecture/blueprints/todoapp/util/DataBindingIdlingResource.kt

class DataBindingIdlingResource : IdlingResource {
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

    override fun getName() = "DataBinding $id"

    /**
     * Sets the fragment from a [FragmentScenario] to be used from [DataBindingIdlingResource].
     */
    fun monitorFragment(fragmentScenario: FragmentScenario<out Fragment>) {
        this.fragmentScenario = fragmentScenario
    }

    fun monitorActivity(activityScenario: ActivityScenario<out Activity>) {
        this.activityScenario = activityScenario
    }

    override fun isIdleNow(): Boolean {
        val idle = !getBindings().any { it.hasPendingBindings() }
        @Suppress("LiftReturnOrAssignment")
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
                throw IllegalStateException("DataBindingIdlingResource is not connected to any " +
                        "fragment or activity scenario! If you are using a test @Rule, please " +
                        "make sure to call monitorFragment or monitorActivity **BEFORE** any Espresso" +
                        "onView or onIdle call")
            }
        }
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        idlingCallbacks.add(callback)
    }

    /**
     * Find all binding classes in all currently available fragments.
     */
    private fun getBindings(): List<ViewDataBinding> {
        val bindings = mutableListOf<ViewDataBinding>()
        fragmentScenario?.onFragment { fragment ->
            bindings += fragment.viewBindings()
        }

        activityScenario?.onActivity {
            val fragments = (it as? FragmentActivity)?.supportFragmentManager
                    ?.fragments ?: listOf()
            bindings += fragments.flatMap { it.viewBindings() }
        }

        return bindings
    }

    private fun Fragment.viewBindings(): List<ViewDataBinding> {
        val bindings = view?.flattenHierarchy()?.mapNotNull { view ->
            DataBindingUtil.getBinding<ViewDataBinding>(view)
        } ?: listOf()
        val childBindings = childFragmentManager.fragments.flatMap { it.viewBindings() }
        return bindings + childBindings
    }

    private fun View.flattenHierarchy(): List<View> = if (this is ViewGroup) {
        listOf(this) + children.map { it.flattenHierarchy() }.flatten()
    } else {
        listOf(this)
    }
}