package com.ustadmobile.core.util.ext

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import androidx.test.core.app.ApplicationProvider
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
        val uriWithSubfolder = documentFile.uri.withSubTreePathFragment(subFolder.name)
        val (uriWithoutSubfolder, subfolderName) = uriWithSubfolder.extractSubTreePath()

        Assert.assertEquals(documentFile.uri, uriWithoutSubfolder)
        Assert.assertEquals(subFolder.name, subfolderName)
    }

    @Test
    fun givenUriWithFragment_whenSubcomponentAdded_shouldMatch() {
        val folderUriWithFragment = documentFile.uri.buildUpon()
            .fragment("foo")
            .build()

        val uriWithSubfolder = folderUriWithFragment.withSubTreePathFragment(subFolder.name)
        val (uriWithoutSubfolder, subfolderName) = uriWithSubfolder.extractSubTreePath()

        Assert.assertEquals(folderUriWithFragment, uriWithoutSubfolder)
        Assert.assertEquals(subFolder.name, subfolderName)
    }

    @Test
    fun givenDocumentFile_whenSubpathAdded_thenCanListSubfolder() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val subSubFolder = File(subFolder, "subsubfolder")
        subSubFolder.mkdirs()
        val fileInSubSubFile = File(subSubFolder, "file.txt")
        fileInSubSubFile.writeText("Hello World")
        println(documentFile.uri.toString())

        val subfolderUri = documentFile.listFiles().first().let {
            documentFile.uri.appendSubTreePath(it.name!!)
        }

        val subfolderDocumentFile = subfolderUri.toDocumentFileIncludingSubpath(context)
        val subSubFolderUri = subfolderDocumentFile.listFiles().first().let {
            subfolderUri.appendSubTreePath(it.name!!)
        }

        val filesInSubSubFolder = subSubFolderUri.toDocumentFileIncludingSubpath(context)
            .listFiles().first()
        Assert.assertEquals("file.txt", filesInSubSubFolder.name)
    }

}