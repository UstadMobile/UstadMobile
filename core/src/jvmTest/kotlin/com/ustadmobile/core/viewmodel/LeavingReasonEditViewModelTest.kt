package com.ustadmobile.core.viewmodel

import app.cash.turbine.test
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.flow.doorFlow
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import org.junit.Test
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.time.Duration.Companion.seconds

class LeavingReasonEditViewModelTest: AbstractMainDispatcherTest() {

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        testViewModel<LeavingReasonEditViewModel> {
            viewModelFactory {
                LeavingReasonEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere { it.fieldsEnabled && it.leavingReason != null }

                viewModel.onEntityChanged(leavingReason = readyState.leavingReason?.shallowCopy {
                    leavingReasonTitle = "Testing problem"
                })

                viewModel.onClickSave()
                cancelAndIgnoreRemainingEvents()
            }

            val db = di.direct.on(activeLearningSpace).instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
            db.doorFlow(arrayOf("LeavingReason")) {
                db.leavingReasonDao().findAllReasonsAsync()
            }.assertItemReceived { list ->
                list.any { it.leavingReasonTitle == "Testing problem" }
            }
        }
    }
}