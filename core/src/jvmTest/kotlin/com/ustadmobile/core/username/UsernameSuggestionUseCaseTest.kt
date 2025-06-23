package com.ustadmobile.core.username

import com.ustadmobile.door.entities.NodeIdAndAuth

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.filterusername.FilterUsernameUseCase
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.ext.use
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals
class UsernameSuggestionUseCaseTest {

    private fun testUsernameSuggestion(
        existingUsernames: List<String>,
        inputName: String,
        expectedUsername: String,
    ) {
        val nodeIdAndAuth = NodeIdAndAuth(Random.nextLong(), randomUuid().toString())
        DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class,
            "jdbc:sqlite::memory:",
            nodeId = nodeIdAndAuth.nodeId
        ).build().use { db ->
            runBlocking {
                db.withDoorTransactionAsync {
                    existingUsernames.forEach {
                        db.personDao().insertAsync(
                            Person().apply { username = it }
                        )
                    }
                }

                val useCase = UsernameSuggestionUseCase(
                    filterUsernameUseCase = FilterUsernameUseCase(),
                    db = db
                )

                val result = useCase(inputName)
                assertEquals(expectedUsername, result)
            }
        }
    }

    @Test
    fun suggestion_should_return_input_when_unique() {
        testUsernameSuggestion(
            existingUsernames = emptyList(),
            inputName = "johndoe",
            expectedUsername = "johndoe"
        )
    }

    @Test
    fun suggestion_should_append_1_when_taken_once() {
        testUsernameSuggestion(
            existingUsernames = listOf("johndoe"),
            inputName = "johndoe",
            expectedUsername = "johndoe1"
        )
    }

    @Test
    fun suggestion_should_increment_until_unique() {
        testUsernameSuggestion(
            existingUsernames = listOf("johndoe", "johndoe1", "johndoe2"),
            inputName = "johndoe",
            expectedUsername = "johndoe3"
        )
    }
}
