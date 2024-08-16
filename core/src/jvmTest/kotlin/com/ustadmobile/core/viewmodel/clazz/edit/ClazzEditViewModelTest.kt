package com.ustadmobile.core.viewmodel.clazz.edit

import app.cash.turbine.test
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueContentEntryImportUseCase
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.flow.doorFlow
import com.ustadmobile.lib.db.entities.SystemPermission
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.util.test.initNapierLog
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.kodein.di.*
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds


class ClazzEditViewModelTest : AbstractMainDispatcherTest() {

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        initNapierLog()
        testViewModel<ClazzEditViewModel> {
            val user = setActiveUser(activeEndpoint)

            viewModelFactory {
                ClazzEditViewModel(di, savedStateHandle)
            }

            activeDb.systemPermissionDao().upsertAsync(
                SystemPermission(
                    spToPersonUid = user.personUid,
                    spPermissionsFlag = PermissionFlags.ADD_COURSE or PermissionFlags.COURSE_EDIT
                )
            )

            extendDi {
                bind<CreateNewClazzUseCase>() with scoped(endpointScope).singleton {
                    CreateNewClazzUseCase(
                        repoOrDb = instance(tag = DoorTag.TAG_REPO)
                    )
                }

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
                    db.clazzDao().findAll()
                }.assertItemReceived(timeout = 5.seconds) { allClazzes ->
                    allClazzes.any {
                        it.clazzName == "Test course" && it.clazzDesc == "Test description"
                    }
                }

                cancelAndIgnoreRemainingEvents()
            }
        }
    }


}