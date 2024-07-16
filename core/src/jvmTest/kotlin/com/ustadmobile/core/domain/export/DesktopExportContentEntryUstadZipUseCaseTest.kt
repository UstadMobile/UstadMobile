package com.ustadmobile.core.domain.export


import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.rmi.server.ExportException
import java.util.zip.ZipFile
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class DesktopExportContentEntryUstadZipUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var contentEntryDao: ContentEntryDao
    private lateinit var useCase: DesktopExportContentEntryUstadZipUseCase
    private lateinit var json: Json

    @Before
    fun setup() {
        contentEntryDao = mock()
        json = Json { prettyPrint = true }
        useCase = DesktopExportContentEntryUstadZipUseCase(contentEntryDao, json)
    }

    @Test
    fun givenFolderWithChildEntries_whenExported_thenZipFileContainsAllEntries() = runBlocking {
        // Given
        val parentEntry = ContentEntry().apply {
            contentEntryUid = 1L
            title = "Parent Folder"
            leaf = false
        }
        val childEntry1 = ContentEntry().apply {
            contentEntryUid = 2L
            title = "Child 1"
            leaf = true
        }
        val childEntry2 = ContentEntry().apply {
            contentEntryUid = 3L
            title = "Child 2"
            leaf = true
        }
        whenever(contentEntryDao.findByUidAsync(1L)).thenReturn(parentEntry)
        whenever(contentEntryDao.getChildrenByParentAsync(1L)).thenReturn(listOf(childEntry1, childEntry2))
        whenever(contentEntryDao.getChildrenByParentAsync(2L)).thenReturn(emptyList())
        whenever(contentEntryDao.getChildrenByParentAsync(3L)).thenReturn(emptyList())

        val destZipFile = tempFolder.newFile("export.zip")

        // When
        val progressList = mutableListOf<ExportProgress>()
        useCase.invoke(1L, destZipFile.absolutePath) { progress ->
            progressList.add(progress)
        }

        // Then
        println("Progress List: $progressList")
        assertTrue(destZipFile.exists())
        assertTrue(destZipFile.length() > 0)

        // Verify progress
        assertEquals(4, progressList.size) // 3 entries + final progress
        assertEquals(1f, progressList.last().progress)

        // Verify ZIP contents
        val entryContents = mutableMapOf<String, String>()
        ZipFile(destZipFile).use { zipFile ->
            for (entry in zipFile.entries()) {
                val content = zipFile.getInputStream(entry).bufferedReader().use { it.readText() }
                entryContents[entry.name] = content
            }
        }

        println("ZIP contents: $entryContents")
        assertEquals(3, entryContents.size)
        listOf(1L, 2L, 3L).forEach { uid ->
            val content = entryContents["$uid.json"]
            assertNotNull("Entry $uid.json not found in ZIP", content)
            val decodedEntry = json.decodeFromString<ContentEntry>(content!!)
            assertEquals(uid, decodedEntry.contentEntryUid)
        }
    }

    @Test
    fun givenSingleContentEntry_whenExported_thenZipFileContainsCorrectJSON() = runBlocking {
        // Given
        val contentEntry = ContentEntry().apply {
            contentEntryUid = 1L
            title = "Test Entry"
            leaf = true
        }
        whenever(contentEntryDao.findByUidAsync(1L)).thenReturn(contentEntry)
        whenever(contentEntryDao.getChildrenByParentAsync(1L)).thenReturn(emptyList())

        val destZipFile = tempFolder.newFile("export.zip")

        // When
        val progressList = mutableListOf<ExportProgress>()
        useCase.invoke(1L, destZipFile.absolutePath) { progress ->
            progressList.add(progress)
        }

        // Then
        println("Progress List: $progressList")
        assertTrue(destZipFile.exists())
        assertTrue(destZipFile.length() > 0)

        // Verify progress
        assertEquals(2, progressList.size) // 1 entry + final progress
        assertEquals(1f, progressList.last().progress)

        // Verify ZIP contents
        ZipFile(destZipFile).use { zipFile ->
            val entry = zipFile.getEntry("1.json")
            assertNotNull(entry)
            val content = zipFile.getInputStream(entry).bufferedReader().use { it.readText() }
            val decodedEntry = json.decodeFromString<ContentEntry>(content)
            assertEquals(contentEntry, decodedEntry)
        }
    }

    @Test
    fun givenNonExistentContentEntry_whenExported_thenThrowsExportException(): Unit = runBlocking {
        // Given
        whenever(contentEntryDao.findByUidAsync(1L)).thenReturn(null)
        val destZipFile = tempFolder.newFile("export.zip")

        // When/Then
        val exception = assertFailsWith<ExportException> {
            useCase.invoke(1L, destZipFile.absolutePath) {}
        }
        assertEquals("Error during export: Content entry not found for UID: 1", exception.message)

        // Verify that findByUidAsync was actually called
        verify(contentEntryDao).findByUidAsync(1L)
    }
}