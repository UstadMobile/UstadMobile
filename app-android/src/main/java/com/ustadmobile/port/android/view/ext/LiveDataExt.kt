package com.ustadmobile.port.android.view.ext

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.ustadmobile.door.RepositoryLoadHelper

/**
 * Get the repository loading status from the LiveData if this is a LiveData wrapped by a repository
 */
val LiveData<*>.repoLoadingStatus: LiveData<RepositoryLoadHelper.RepoLoadStatus>?
    get() {
        if(this is RepositoryLoadHelper<*>.LiveDataWrapper2<*>) {
            return this.loadingStatus
        }else {
            return null
        }
    }

/**
 * Safety wrapper to use a Fragment's viewLifecycleOwner to observe LiveData. If a user is moving
 * from one screen to another, and the data loads after the view is destroyed, this will result
 * in a crash. This function will check to make sure that the fragment's view is not null, and
 * will only proceed to observe if the view is ready
 *
 * @param fragment the fragment where we want to use it's viewLifecycleOwner to observe this LiveData
 * @param observer the observer as per the normal LiveData#observe
 */
fun <T> LiveData<T>.observeIfFragmentViewIsReady(fragment: Fragment, observer: Observer<in T>)  {
    if(fragment.view != null) {
        observe(fragment.viewLifecycleOwner, observer)
    }
}
