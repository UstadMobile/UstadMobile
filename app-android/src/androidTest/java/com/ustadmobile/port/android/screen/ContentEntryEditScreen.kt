package com.ustadmobile.port.android.screen

import android.net.Uri
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.android.view.ContentEntryEdit2Fragment
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.waitUntilWithFragmentScenario
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import junit.framework.Assert
import java.io.File

object ContentEntryEditScreen : KScreen<ContentEntryEditScreen>() {


    override val layoutId: Int?
        get() = R.layout.fragment_content_entry_edit2
    override val viewClass: Class<*>?
        get() = ContentEntryEdit2Fragment::class.java

    val importButton = KButton { withId(R.id.content_entry_select_file)}

    val storageOption = KTextView { withId(R.id.storage_option)}

    val entryTitleTextInput = KTextInputLayout { withId(R.id.entry_title)}

    val descTextInput = KTextInputLayout{withId(R.id.entry_description)}

    fun createEntryFromFile(fileName: String, isZipped: Boolean = true,
                                    systemImplNavRule : SystemImplTestNavHostRule,
                                    dbRule: UmAppDatabaseAndroidClientRule): Container {
        val containerTmpDir = UmFileUtilSe.makeTempDir("containerTmpDir","${System.currentTimeMillis()}")
        val testFile = File.createTempFile("contentEntryEdit", fileName, containerTmpDir)
        val input = javaClass.getResourceAsStream("/com/ustadmobile/app/android/$fileName")
        testFile.outputStream().use { input?.copyTo(it) }
        val expectedUri = Uri.fromFile(testFile)

        val registry = object : ActivityResultRegistry() {
            override fun <I, O> invoke(
                    requestCode: Int,
                    contract: ActivityResultContract<I, O>,
                    input: I,
                    options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedUri)
            }
        }

        val fragmentScenario  = with(launchFragmentInContainer(
                fragmentArgs = bundleOf(UstadView.ARG_LEAF to true.toString(),
                        UstadView.ARG_PARENT_ENTRY_UID to 10000L.toString()), themeResId = R.style.UmTheme_App) {
            ContentEntryEdit2Fragment(registry).also {
                it.installNavController(systemImplNavRule.navController)
            } }) { onFragment { fragment -> fragment.handleFileSelection()}
        }

        importButton{
            isDisplayed()
        }

        if(!isZipped){
            entryTitleTextInput{
                edit{
                    clearText()
                    typeText("Dummy Title")
                }
            }
        }

        val repo = dbRule.repo as DoorDatabaseSyncRepository
        repo.clientId
        fragmentScenario.clickOptionMenu(R.id.menu_done)

        val entries = dbRule.db.contentEntryDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario, 15000) {
            it.isNotEmpty()
        }

        val container = dbRule.db.containerDao.getMostRecentContainerForContentEntryLive(entries?.first()?.contentEntryUid!!)
                .waitUntilWithFragmentScenario(fragmentScenario, timeout = 15000){it != null}

        Assert.assertTrue("Entry's data set and is a leaf", entries.first().title != null && entries.first().leaf)

        containerTmpDir.deleteRecursively()

        return container
    }

}