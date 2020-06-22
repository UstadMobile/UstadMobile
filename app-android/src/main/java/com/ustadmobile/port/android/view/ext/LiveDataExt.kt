package com.ustadmobile.port.android.view.ext

import androidx.lifecycle.LiveData
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