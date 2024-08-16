package com.ustadmobile.core.domain.contententry.delete

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListSelectedItem
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.DeletedItem
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteContentEntryParentChildJoinUseCaseTest {

    @Test
    fun givenEntryToDelete_whenInvoked_willMarkContentParentChildJoinAsDeletedAndInsertDeletedItem() {
        val db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, "jdbc:sqlite::memory:", nodeId = 1L
        ).build()

        runBlocking {
            val contentEntryTitle = "Title"
            val contentEntryParentChildJoinUid = db.withDoorTransactionAsync {
                val childContentEntryUid = db.contentEntryDao().insertAsync(
                    ContentEntry().apply {
                        title = contentEntryTitle
                    }
                )

                val contentEntryParentChildJoinUid = db.contentEntryParentChildJoinDao().insertAsync(
                    ContentEntryParentChildJoin(
                        cepcjParentContentEntryUid = 42L,
                        cepcjChildContentEntryUid = childContentEntryUid,
                    )
                )

                val useCase = DeleteContentEntryParentChildJoinUseCase(db)

                useCase(
                    entries = setOf(
                        ContentEntryListSelectedItem(
                            contentEntryUid = childContentEntryUid,
                            contentEntryParentChildJoinUid = contentEntryParentChildJoinUid,
                            parentContentEntryUid = 42L
                        )
                    ),
                    activeUserPersonUid = 1L
                )

                contentEntryParentChildJoinUid
            }

            val joinInDb = db.contentEntryParentChildJoinDao()
                .findByUid(contentEntryParentChildJoinUid)
            assertTrue(joinInDb?.cepcjDeleted ?: false)

            val delItemsInDb = db.deletedItemDao().findByTableIdAndEntityUid(
                ContentEntryParentChildJoin.TABLE_ID,
                contentEntryParentChildJoinUid
            )
            assertEquals(1, delItemsInDb.size)

            assertEquals(contentEntryTitle, delItemsInDb.first().delItemName)
            assertEquals(DeletedItem.STATUS_PENDING, delItemsInDb.first().delItemStatus)
        }

    }

}