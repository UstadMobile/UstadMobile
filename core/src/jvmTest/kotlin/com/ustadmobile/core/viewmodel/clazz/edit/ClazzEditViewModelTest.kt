package com.ustadmobile.core.viewmodel.clazz.edit

import app.cash.turbine.test
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueContentEntryImportUseCase
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.flow.doorFlow
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.util.test.initNapierLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert
import org.kodein.di.*
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds


class ClazzEditViewModelTest : AbstractMainDispatcherTest() {

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        initNapierLog()
        testViewModel<ClazzEditViewModel> {
            viewModelFactory {
                ClazzEditViewModel(di, savedStateHandle)
            }

            extendDi {
                bind<ClazzLogCreatorManager>() with singleton {
                    mock { }
                }

                bind<EnqueueContentEntryImportUseCase>() with scoped(endpointScope).singleton {
                    mock { }
                }

                bind<EnqueueSavePictureUseCase>() with scoped(endpointScope).singleton {
                    mock { }
                }
            }

            val readyAppUiState = withTimeout(5000) {
                viewModel.appUiState.filter {
                    it.actionBarButtonState.visible
                }.first()
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val state = awaitItemWhere { it.fieldsEnabled }
                viewModel.onEntityChanged(state.entity?.shallowCopy {
                    clazzName = "Test course"
                    clazzDesc = "Test description"
                })

                readyAppUiState.actionBarButtonState.onClick()

                val db = di.direct.on(activeEndpoint).instance<UmAppDatabase>(tag = DoorTag.TAG_DB)

                db.doorFlow(arrayOf("Clazz")) {
                    db.clazzDao.findAll()
                }.assertItemReceived(timeout = 5.seconds) { allClazzes ->
                    allClazzes.any {
                        it.clazzName == "Test course" && it.clazzDesc == "Test description"
                    }
                }

                val entitySaved = db.clazzDao.findAll().first { it.clazzName == "Test course" }

                val clazzScopedGrants = runBlocking {
                    db.scopedGrantDao.findByTableIdAndEntityUid(Clazz.TABLE_ID, entitySaved.clazzUid)
                }
                val teacherGroup = db.personGroupDao.findByUid(entitySaved.clazzTeachersPersonGroupUid)
                Assert.assertNotNull("Teacher group exists", teacherGroup)
                val teacherGrant = clazzScopedGrants.find {
                    it.scopedGrant?.sgGroupUid == entitySaved.clazzTeachersPersonGroupUid
                }
                Assert.assertEquals("Teacher grant has default teacher permissions",
                    Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT, teacherGrant?.scopedGrant?.sgPermissions)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenExistingClazz_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        testViewModel<ClazzEditViewModel> {
            val testEntity = Clazz().apply {
                clazzName = "Spelling Clazz"
                clazzStartTime = DateTime(2020, 10, 10).unixMillisLong
                isClazzActive = true
                clazzUid = activeDb.clazzDao.insert(this)
            }


            viewModelFactory {
                savedStateHandle[ARG_ENTITY_UID] = testEntity.clazzUid.toString()
                ClazzEditViewModel(di, savedStateHandle)
            }

            extendDi {
                bind<ClazzLogCreatorManager>() with singleton {
                    mock { }
                }

                bind<EnqueueContentEntryImportUseCase>() with scoped(endpointScope).singleton {
                    mock { }
                }

                bind<EnqueueSavePictureUseCase>() with scoped(endpointScope).singleton {
                    mock { }
                }
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val state = awaitItemWhere { it.fieldsEnabled }

                viewModel.onEntityChanged(state.entity?.shallowCopy {
                    clazzName = "New Spelling Clazz"
                })

                cancelAndIgnoreRemainingEvents()
            }

            viewModel.onClickSave()

            //This should **NOT** be needed -however it seems that the doorFlow needs checked for a potential race condition
            delay(1000)

            activeDb.doorFlow(arrayOf("Clazz")) {
                activeDb.clazzDao.findAll()
            }.assertItemReceived(timeout = 5.seconds) { allItems ->
                allItems.any { it.clazzName == "New Spelling Clazz" }
            }
        }
    }

}