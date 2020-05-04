package com.ustadmobile.port.android.util.ext

import android.os.Bundle
import androidx.navigation.NavController
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView

fun NavController.currentBackStackEntrySavedStateMap() = this.currentBackStackEntry?.savedStateHandle?.toStringMap()

