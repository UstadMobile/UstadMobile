package com.ustadmobile.core.viewmodel.respect.respectassignment.detail

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.lib.db.entities.respect.RespectAssignment
import org.kodein.di.DI

class RespectAssignmentDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : DetailViewModel<RespectAssignment>(di, savedStateHandle, DEST_NAME) {



    companion object {

        const val DEST_NAME = "RespectAssignmentDetail"

    }
}