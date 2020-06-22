package com.ustadmobile.test.rules

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ActivityScenario

inline fun <reified F: Fragment> FragmentScenario<F>.withScenarioIdlingResourceRule(idlingResourceRule: ScenarioIdlingResourceRule<*>) : FragmentScenario<F> {
    val idlingResource = idlingResourceRule.idlingResource
    if(idlingResource is ScenarioIdlingResource) {
        idlingResource.monitorFragment(this)
    }else {
        throw IllegalArgumentException("IdlingResourceRule does not implement ScenarioIdlingResource!")
    }

    return this
}

inline fun <reified A: Activity> ActivityScenario<A>.withScenarioIdlingResourceRule(idlingResourceRule: ScenarioIdlingResourceRule<*>) : ActivityScenario<A> {
    val idlingResource = idlingResourceRule.idlingResource
    if(idlingResource is ScenarioIdlingResource) {
        idlingResource.monitorActivity(this)
    }else {
        throw IllegalArgumentException("IdlingResourceRule does not implement ScenarioIdlingResource!")
    }

    return this
}


interface ScenarioIdlingResource  {

    fun monitorActivity(activityScenario: ActivityScenario<out Activity>?)

    fun monitorFragment(fragmentScenario: FragmentScenario<out Fragment>?)

}