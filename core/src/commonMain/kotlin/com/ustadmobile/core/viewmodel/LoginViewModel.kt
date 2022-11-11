package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.PersonConstants
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.launch
import org.kodein.di.on

data class LoginUiState(
    val isEmptyPassword: Boolean = false,
    val usernameErrorMessage: String = "",
    val passwordErrorMessage: String = "",
    val versionInfo: String = "v42",
    val isEmptyUsername: Boolean = false,
    val inProgress: Boolean = false,
    val createAccountVisible: Boolean = false,
    val connectAsGuestVisible: Boolean = false,
    val loginIntentMessage: String = ""
)