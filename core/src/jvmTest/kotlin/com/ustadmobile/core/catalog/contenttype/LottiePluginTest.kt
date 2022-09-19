package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI

class LottiePluginTest {

    @JvmField
    @Rule
    val tmpFolder = TemporaryFolder()

    private val context = Any()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase


    @Test
    fun givenValidLottieFile_whnExtractMetaDataCalled_thenShouldExtractTitle() {
        Assert.assertEquals(2 + 2, 5)

    }


}