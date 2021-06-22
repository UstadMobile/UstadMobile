package com.ustadmobile.redux

import redux.RAction

data class ReduxSnackBarState(var message: String? = null,
                              var actionLabel: String? = null,
                              var onClick: () -> Unit = {}): RAction
