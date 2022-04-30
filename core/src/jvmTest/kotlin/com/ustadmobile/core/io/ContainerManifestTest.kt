package com.ustadmobile.core.io

import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerManifestTest {

    @Test
    fun givenManifestCreated_whenSavedToString_thenParsed_shouldBeTheSame() {
        val manifest = ContainerManifest(1, 42,
            listOf(ContainerManifest.Entry("META-INF/container.xml",
                "fefe1010fe", "sha256-abc", 1000)))
        val manifestStr = manifest.toManifestString()


        val parsedManifest = ContainerManifest.parseFromString(manifestStr)

        assertEquals(manifest, parsedManifest, "Parsed manifest is the same as original")
    }

}