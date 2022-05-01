package com.ustadmobile.core.io

import com.ustadmobile.door.util.systemTimeInMillis
import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerManifestTest {

    @Test
    fun givenManifestCreated_whenSavedToString_thenParsed_shouldBeTheSame() {
        val manifest = ContainerManifest(1, 42,
            listOf(ContainerManifest.Entry("META-INF/container.xml",
                "fefe1010fe", "sha256-abc", 42L, 1000,
                2000, 1, systemTimeInMillis())))
        val manifestStr = manifest.toManifestString()


        val parsedManifest = ContainerManifest.parseFromString(manifestStr)

        assertEquals(manifest, parsedManifest, "Parsed manifest is the same as original")
    }

    @Test
    fun givenManifestCreated_whenConvertedToContainerEntriesThenBack_shouldBeTheSame() {
        val manifest = ContainerManifest(1, 42,
            listOf(ContainerManifest.Entry("META-INF/container.xml",
                "fefe1010fe", "sha256-abc", 42L, 1000,
                2000, 1, systemTimeInMillis())))

        val containerEntries = manifest.toContainerEntryList()

        val createdManifest = ContainerManifest.fromContainerEntryWithContainerEntryFiles(containerEntries)
        assertEquals(manifest, createdManifest,
            "Manifest created from entries is the same as original")
    }


}