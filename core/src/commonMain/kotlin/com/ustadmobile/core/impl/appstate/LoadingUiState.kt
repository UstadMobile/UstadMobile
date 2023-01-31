package com.ustadmobile.core.impl.appstate

/**
 *Represents the loading state that is displayed in the action bar at the top of the app. Currently
 * only NOT_LOADING and INDETERMINATE are supported, but support for determinate progress will be
 * added.
 */
data class LoadingUiState(
    val loadingState: State = State.NOT_LOADING,
) {

    enum class State {
        NOT_LOADING, INDETERMINATE
    }

    companion object {

        val INDETERMINATE = LoadingUiState(State.INDETERMINATE)

        val NOT_LOADING = LoadingUiState(State.NOT_LOADING)

    }

}