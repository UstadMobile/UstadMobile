package com.ustadmobile.core.domain.peerreviewallocation

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.math.min

class UpdatePeerReviewAllocationUseCaseTest {

    val systemImpl = mock<UstadMobileSystemImpl> {
        on { getString(MR.strings.group_number) }.thenReturn("Group %1\$s")
    }

    private fun assertAllocationsAreValid(
        submitterUids: List<Long>,
        numReviewsPerSubmission: Int,
        allocations: List<PeerReviewerAllocation>
    ) {
        val expectedNumberOfReviews = min(numReviewsPerSubmission, submitterUids.size - 1)

        submitterUids.forEach { personUid ->
            val itemsThisPersonMarks = allocations.filter { it.praMarkerSubmitterUid == personUid }

            //Should have the expected number of items to mark: if the maths is not even, then some
            // might have one fewer items to mark
            assertTrue(
                itemsThisPersonMarks.size >= (expectedNumberOfReviews - 1) &&
                    itemsThisPersonMarks.size <= expectedNumberOfReviews,
                "Number of reviews "
            )

            assertFalse(itemsThisPersonMarks.any { it.praToMarkerSubmitterUid == personUid },
                "Peer should not be assigned to mark themselves")

            assertEquals(
                itemsThisPersonMarks.size,
                itemsThisPersonMarks.distinctBy { it.praToMarkerSubmitterUid }.size,
                "Should not be marking any other peer more than once")
        }
    }

    @Test
    fun givenNoSubmitters_whenInvoked_thenShouldReturnEmptyList() {
        val db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, dbUrl = "jdbc:sqlite::memory:",
            nodeId = 1L
        ).build()

        val updatePraUseCase = UpdatePeerReviewAllocationUseCase(db, systemImpl)
        runBlocking {
            val allocations = updatePraUseCase(emptyList(), 0, 1, 1, 2, true)
            assertEquals(0, allocations.size)
        }
    }

    private fun UmAppDatabase.insertPeopleIntoClass(clazzUid: Long, personUids: List<Long>) {
        personDao().insertList(personUids.map {
            Person().apply {
                firstNames = "FirstName$it"
                lastName = "LastName$it"
                personUid = it.toLong()
            }
        })

        clazzEnrolmentDao().insertList(personUids.map {
            ClazzEnrolment().apply {
                clazzEnrolmentPersonUid = it.toLong()
                clazzEnrolmentClazzUid = clazzUid
                clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT
            }
        })
    }

    @Test
    fun givenClazzWithSubmittersWithNoExistingAllocations_whenInvoked_thenShouldAllocate() {
        val db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, dbUrl = "jdbc:sqlite::memory:",
            nodeId = 1L
        ).build()

        val clazzUid = 42L
        val numReviewsPerSubmission = 2

        db.insertPeopleIntoClass(42L, (1L..50L).toList())

        val updatePraUseCase = UpdatePeerReviewAllocationUseCase(db, systemImpl)


        runBlocking {
            val allocations = updatePraUseCase(emptyList(), 0, clazzUid, 1, numReviewsPerSubmission, true)
            assertAllocationsAreValid((1L..50L).toList(), numReviewsPerSubmission, allocations)
        }
    }

    @Test
    fun givenClazzWithSubmittersWithExistingAllocations_whenNumReviewersReduced_thenShouldTruncateList() {
        val db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, dbUrl = "jdbc:sqlite::memory:",
            nodeId = 1L
        ).build()

        val clazzUid = 42L
        val numReviewsPerSubmission = 1
        db.insertPeopleIntoClass(42L, (1L..50L).toList())

        val existingAllocations = (1L..50L).map {
            PeerReviewerAllocation().apply {
                praUid = it
                praToMarkerSubmitterUid = it
                praMarkerSubmitterUid = if(it < 60L) it + 1 else 1
            }
        } +  (1L..50L).map {
            PeerReviewerAllocation().apply {
                praUid = it
                praToMarkerSubmitterUid = it
                praMarkerSubmitterUid = if(it < 59L) it + 2 else (it - 60) + 2
            }
        }

        val updatePraUseCase = UpdatePeerReviewAllocationUseCase(db, systemImpl)


        runBlocking {
            val allocations = updatePraUseCase(existingAllocations, 0, clazzUid, 1, numReviewsPerSubmission, true)
            assertAllocationsAreValid((1L..50L).toList(), numReviewsPerSubmission, allocations)
        }
    }

    @Test
    fun givenClazzWithSubmittersWithExistingAllocations_whenNumReviewersIncreased_thenShouldAllocateRemainder() {
        val db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, dbUrl = "jdbc:sqlite::memory:",
            nodeId = 1L
        ).build()

        val clazzUid = 42L
        val numReviewsPerSubmission = 2
        db.insertPeopleIntoClass(42L, (1L..60L).toList())

        val existingAllocations = (1L..60L).map {
            PeerReviewerAllocation().apply {
                praUid = it
                praToMarkerSubmitterUid = it
                praMarkerSubmitterUid = if(it < 60L) it + 1 else 1
            }
        }

        val updatePraUseCase = UpdatePeerReviewAllocationUseCase(db, systemImpl)
        runBlocking {
            val allocations = updatePraUseCase(existingAllocations, 0, clazzUid, 1, numReviewsPerSubmission, true)
            //All existing allocations should be untouched
            existingAllocations.forEach { existingAllocation ->
                assertEquals(existingAllocation, allocations.single { it.praUid == existingAllocation.praUid })
            }
            assertAllocationsAreValid((1L..60L).toList(), numReviewsPerSubmission, allocations)
        }
    }

    @Test
    fun givenClazzWithSubmitters_whenTooManyReviewsSet_thenShouldRunThroughWithSomeAllocationsEmpty() {
        val db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, dbUrl = "jdbc:sqlite::memory:", nodeId = 1L,
        ).build()

        val clazzUid = 42L
        val numReviewsPerSubmission = 6

        db.insertPeopleIntoClass(42L, (1L..5).toList())

        val updatePraUseCase = UpdatePeerReviewAllocationUseCase(db, systemImpl)

        runBlocking {
            val allocations = updatePraUseCase(emptyList(), 0, clazzUid, 1, numReviewsPerSubmission, true)
            assertAllocationsAreValid((1L..5L).toList(), numReviewsPerSubmission, allocations)
            assertTrue(allocations.count { it.praMarkerSubmitterUid == 0L } > 0,
                "As more reviews per submission have been requested than peers are available, some allocations will be unassigned")
        }
    }


}