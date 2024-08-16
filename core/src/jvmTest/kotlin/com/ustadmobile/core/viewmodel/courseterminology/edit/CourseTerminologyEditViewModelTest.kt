package com.ustadmobile.core.viewmodel.courseterminology.edit

import app.cash.turbine.test
import com.ustadmobile.core.controller.TerminologyKeys.TEACHER_KEY
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.encodeStringMapToString
import com.ustadmobile.core.util.ext.toTerminologyEntries
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.flow.doorFlow
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class CourseTerminologyEditViewModelTest : AbstractMainDispatcherTest() {

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        testViewModel<CourseTerminologyEditViewModel> {
            viewModelFactory {
                CourseTerminologyEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val state = awaitItemWhere {
                    it.fieldsEnabled && it.entity != null
                }

                viewModel.onEntityChanged(state.entity?.shallowCopy {
                    ctTitle = "University"
                })

                viewModel.onTerminologyTermChanged(
                    state.terminologyTermList.first{ it.id == TEACHER_KEY }.copy(
                        term = "Professor"
                    )
                )

                viewModel.onClickSave()

                cancelAndIgnoreRemainingEvents()
            }

            activeDb.doorFlow(arrayOf("CourseTerminology")) {
                activeDb.courseTerminologyDao().findAllCourseTerminologyList()
            }.assertItemReceived(timeout = 5.seconds) { list ->
                list.any {
                    val terms = it.toTerminologyEntries(json, systemImpl)

                    terms.find { it.id ==  TEACHER_KEY }?.term == "Professor" &&
                        it.ctTitle == "University"
                }
            }
        }
    }

    @Test
    fun givenExistingCourseTerminology_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        testViewModel<CourseTerminologyEditViewModel> {
            val testEntity = CourseTerminology().apply {
                ctTitle = "University"
                ctTerminology = json.encodeStringMapToString(
                    mapOf(TEACHER_KEY to "Professor")
                )
                ctUid = activeDb.courseTerminologyDao().insertAsync(this)
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testEntity.ctUid.toString()

                CourseTerminologyEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val state = awaitItemWhere {
                    it.fieldsEnabled && it.entity != null
                }
                assertEquals("University", state.entity?.ctTitle)
                assertEquals("Professor", state.terminologyTermList.find {
                    it.id == TEACHER_KEY
                }?.term)

                viewModel.onTerminologyTermChanged(state.terminologyTermList.first {
                    it.id == TEACHER_KEY
                }.copy(term = "Senior Professor"))
                viewModel.onClickSave()
                cancelAndIgnoreRemainingEvents()
            }


            activeDb.doorFlow(arrayOf("CourseTerminology")) {
                activeDb.courseTerminologyDao().findAllCourseTerminologyList()
            }.assertItemReceived(timeout = 5.seconds) { list ->
                list.any {
                    val terms = it.toTerminologyEntries(json, systemImpl)

                    terms.find { it.id ==  TEACHER_KEY }?.term == "Senior Professor" &&
                        it.ctTitle == "University"
                }
            }
        }
    }



}