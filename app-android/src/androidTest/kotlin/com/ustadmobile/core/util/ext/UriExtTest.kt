package com.ustadmobile.core.util.ext

import androidx.documentfile.provider.DocumentFile
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class UriExtTest {

    @JvmField
    @Rule
    var tempFolder = TemporaryFolder()

    lateinit var folder: File

    lateinit var subFolder: File

    lateinit var documentFile: DocumentFile

    @Before
    fun setup() {
        folder = tempFolder.newFolder("tmp")
        subFolder = File(folder, "subfolder")
        subFolder.mkdirs()
        documentFile = DocumentFile.fromFile(folder)
    }


    @Test
    fun givenUri_whenSubcomponentAdded_shouldMatch() {
        val uriWithSubfolder = documentFile.uri.addSubTreePath(subFolder.name)
        val (uriWithoutSubfolder, subfolderName) = uriWithSubfolder.extractSubTreePath()

        Assert.assertEquals(documentFile.uri, uriWithoutSubfolder)
        Assert.assertEquals(subFolder.name, subfolderName)
    }

    @Test
    fun givenUriWithFragment_whenSubcomponentAdded_shouldMatch() {
        val folderUriWithFragment = documentFile.uri.buildUpon()
            .fragment("foo")
            .build()

        val uriWithSubfolder = folderUriWithFragment.addSubTreePath(subFolder.name)
        val (uriWithoutSubfolder, subfolderName) = uriWithSubfolder.extractSubTreePath()

        Assert.assertEquals(folderUriWithFragment, uriWithoutSubfolder)
        Assert.assertEquals(subFolder.name, subfolderName)
    }

}