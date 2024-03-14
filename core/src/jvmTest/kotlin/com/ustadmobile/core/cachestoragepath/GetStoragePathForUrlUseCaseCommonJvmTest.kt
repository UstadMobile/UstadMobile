package com.ustadmobile.core.cachestoragepath

import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCaseCommonJvm
import com.ustadmobile.core.util.newTestHttpClient
import com.ustadmobile.core.util.newTestOkHttpClient
import com.ustadmobile.core.util.newTestUstadCache
import com.ustadmobile.libcache.UstadCache
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.BeforeTest
import kotlin.test.Test

class GetStoragePathForUrlUseCaseCommonJvmTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var cache: UstadCache

    private lateinit var httpClient: HttpClient

    private lateinit var okHttpClient: OkHttpClient

    @BeforeTest
    fun setup() {
        val json = Json { encodeDefaults = true }
        cache = newTestUstadCache(temporaryFolder)
        okHttpClient = newTestOkHttpClient(temporaryFolder, cache, json)
        httpClient = okHttpClient.newTestHttpClient(json)

    }

    @Test
    fun givenRequestForUrl_whenInvoked_thenWillProvideValidPath() {
        val getStoragePathUseCase = GetStoragePathForUrlUseCaseCommonJvm(
            okHttpClient = okHttpClient,
            cache = cache,
            tmpDir = temporaryFolder.newFolder(),
        )
    }
}