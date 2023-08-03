package com.ustadmobile.core.viewmodel.schedule.edit

import app.cash.turbine.test
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds


@Suppress("RemoveExplicitTypeArguments")
class ScheduleEditViewModelTest {

    @Test
    fun givenScheduleHasNoStartTime_whenClickSave_thenShouldShowError() {
        testViewModel<ScheduleEditViewModel> {
            viewModelFactory {
                ScheduleEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val state = awaitItemWhere {
                    it.fieldsEnabled && it.entity != null
                }

                viewModel.onEntityChanged(state.entity?.shallowCopy {
                    sceduleStartTime = 0L
                    scheduleEndTime = 6 * MS_PER_HOUR.toLong() //6am
                })

                viewModel.onClickSave()

                val updatedState = awaitItemWhere { it.fromTimeError != null }
                assertEquals(systemImpl.getString(MessageID.field_required_prompt),
                    updatedState.fromTimeError)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenScheduleHasNoEndTime_whenClickSave_thenShouldShowError() {
        testViewModel<ScheduleEditViewModel> {
            viewModelFactory {
                ScheduleEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val state = awaitItemWhere { it.fieldsEnabled && it.entity != null}
                viewModel.onEntityChanged(state.entity?.shallowCopy {
                    sceduleStartTime = 6 * MS_PER_HOUR.toLong()
                    scheduleEndTime = 0L
                })

                viewModel.onClickSave()

                val updatedState = awaitItemWhere { it.toTimeError != null }
                assertEquals(systemImpl.getString(MessageID.field_required_prompt),
                    updatedState.toTimeError)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenScheduleStartAfterEndTime_whenClickSave_thenShouldShowError() {
        testViewModel<ScheduleEditViewModel> {
            viewModelFactory {
                ScheduleEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val state = awaitItemWhere{ it.fieldsEnabled && it.entity != null }
                viewModel.onEntityChanged(state.entity?.shallowCopy {
                    sceduleStartTime  = 6 * MS_PER_HOUR.toLong()
                    scheduleEndTime = 5 * MS_PER_HOUR.toLong()
                })

                viewModel.onClickSave()

                val updatedState = awaitItemWhere { it.toTimeError != null }
                assertEquals(systemImpl.getString(MessageID.end_is_before_start_error),
                    updatedState.toTimeError)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenValidSchedule_whenClickSave_thenShouldFinishWithResult() {
        testViewModel<ScheduleEditViewModel> {
            savedStateHandle[UstadView.ARG_RESULT_DEST_KEY] = "schedule"
            savedStateHandle[UstadView.ARG_RESULT_DEST_VIEWNAME] = ClazzEdit2View.VIEW_NAME

            viewModelFactory {
                ScheduleEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5000.seconds) {
                val state = awaitItemWhere { it.fieldsEnabled && it.entity != null }
                viewModel.onEntityChanged(state.entity?.shallowCopy {
                    sceduleStartTime = 6 * MS_PER_HOUR.toLong()
                    scheduleEndTime = 7 * MS_PER_HOUR.toLong()
                })

                viewModel.onClickSave()

                verify(navResultReturner).sendResult(argWhere {
                    it.key == savedStateHandle[UstadView.ARG_RESULT_DEST_KEY] &&
                        it.result.let {
                            it is Schedule &&
                                it.sceduleStartTime == 6 * MS_PER_HOUR.toLong() &&
                                it.scheduleEndTime == 7 * MS_PER_HOUR.toLong() &&
                                it.scheduleUid != 0L
                        }
                })

                cancelAndIgnoreRemainingEvents()
            }
        }
    }


}