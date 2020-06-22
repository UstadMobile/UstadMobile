package com.ustadmobile.test.rules

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class ScenarioIdlingResourceRule<T: IdlingResource>(val idlingResource: T) : TestWatcher() {

    override fun starting(description: Description?) {
        IdlingRegistry.getInstance().register(idlingResource)
    }


    override fun finished(description: Description?) {
        IdlingRegistry.getInstance().unregister(idlingResource)
        if(idlingResource is ScenarioIdlingResource) {
            idlingResource.monitorActivity(null)
            idlingResource.monitorFragment(null)
        }
    }


}