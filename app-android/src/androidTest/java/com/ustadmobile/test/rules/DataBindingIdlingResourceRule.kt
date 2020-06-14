package com.ustadmobile.test.rules

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingRegistry
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import org.junit.rules.TestWatcher
import org.junit.runner.Description

inline fun <reified F: Fragment> FragmentScenario<F>.withDataBindingIdlingResource(idlingResourceRule: DataBindingIdlingResourceRule) : FragmentScenario<F> {
    idlingResourceRule.idlingResource.monitorFragment(this)
    return this
}

inline fun <reified A: Activity> ActivityScenario<A>.withDataBindingIdlingResource(idlingResourceRule: DataBindingIdlingResourceRule) : ActivityScenario<A> {
    idlingResourceRule.idlingResource.monitorActivity(this)
    return this
}

class DataBindingIdlingResourceRule : TestWatcher() {

    private var _idlingResource: DataBindingIdlingResource? = null

    val idlingResource: DataBindingIdlingResource
        get() = _idlingResource ?: throw IllegalStateException("Can only be accessed during a test")

    override fun finished(description: Description?) {
        IdlingRegistry.getInstance().unregister(idlingResource)
        _idlingResource = null
    }

    override fun starting(description: Description?) {
        _idlingResource = DataBindingIdlingResource().also {
            IdlingRegistry.getInstance().register(it)
        }
    }
}