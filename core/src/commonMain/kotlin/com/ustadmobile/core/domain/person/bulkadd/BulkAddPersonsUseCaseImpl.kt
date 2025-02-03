package com.ustadmobile.core.domain.person.bulkadd

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.AlreadyEnroledInClassException
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import com.ustadmobile.core.util.ext.duplicates
import com.ustadmobile.core.util.ext.toLocalMidnight
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class BulkAddPersonsUseCaseImpl(
    private val addNewPersonUseCase: AddNewPersonUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePhoneNumUseCase: PhoneNumValidatorUseCase,
    private val authManager: AuthManager,
    private val enrolUseCase: EnrolIntoCourseUseCase,
    private val createNewClazzUseCase: CreateNewClazzUseCase,
    private val activeDb: UmAppDatabase,
    private val activeRepo: UmAppDatabase?,
): BulkAddPersonsUseCase {

    private fun String?.parseCourseNames(): List<String> {
        return this?.split(";")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() } ?: emptyList()
    }

    data class EnrolmentResult(
        val errors: List<String>
    )

    /**
     *
     */
    private suspend fun processEnrolments(
        personUid: Long,
        courseNameListCol: String?,
        role: Int,
        courseUidMap: Map<String, Clazz>,
    ) : EnrolmentResult{
        val errors = mutableListOf<String>()
        courseNameListCol.parseCourseNames().forEach { clazzName ->
            courseUidMap[clazzName]?.also { clazz ->
                try {
                    enrolUseCase(
                        enrolment = ClazzEnrolment(
                            clazzUid = clazz.clazzUid,
                            personUid = personUid,
                            role = role
                        ),
                        timeZoneId = clazz.clazzTimeZone ?: "UTC"
                    )
                }catch(e: AlreadyEnroledInClassException) {
                    //do nothing
                }catch(e: Throwable) {
                    Napier.d("BulkAddPersonsUseCase: Exception enrolling $personUid into $clazzName")
                    errors += ("Exception enrolling $personUid into $clazzName: $e")
                }
            }
        }

        return EnrolmentResult(errors = errors.toList())
    }

    override suspend fun invoke(
        csv: String,
        onProgress: BulkAddPersonsUseCase.BulkAddOnProgress,
    ): BulkAddPersonsUseCase.BulkAddUsersResult {
        val csvData = csvReader {
            autoRenameDuplicateHeaders = true
        }.readAllWithHeader(csv)
        val errors = mutableListOf<BulkAddPersonsDataError>()

        if(csvData.isEmpty())
            throw IllegalArgumentException("No rows")

        val columnNames = csvData.first().keys
        val missingRequiredColNames = REQUIRED_COLUMNS.filter {
            it !in columnNames
        }

        if(missingRequiredColNames.isNotEmpty()) {
            throw IllegalArgumentException("Missing columns: ${missingRequiredColNames.joinToString()}")
        }

        val allCourseNames = mutableSetOf<String>()
        val allUsernames = mutableListOf<Pair<String, Int>>()

        //validate rows
        csvData.forEachIndexed { index, row ->
            val lineNum = index + 1
            //Required fields
            val username = row[HEADER_USERNAME]!!
            if(username.isBlank())
                errors += BulkAddPersonsDataError(lineNum, HEADER_USERNAME, username)

            allUsernames += username to lineNum

            val password = row[HEADER_PASSWORD]!!
            if(password.isBlank() || password.length < 6)
                errors += BulkAddPersonsDataError(lineNum, HEADER_PASSWORD, password)

            val firstNames = row[HEADER_FIRSTNAMES]!!
            if(firstNames.isBlank())
                errors += BulkAddPersonsDataError(lineNum, HEADER_FIRSTNAMES, firstNames)

            val familyNames = row[HEADER_FAMILYNAME]!!
            if(familyNames.isBlank()) {
                errors += BulkAddPersonsDataError(lineNum, HEADER_FAMILYNAME, familyNames)
            }

            val sex = row[HEADER_SEX]!!
            if(sex.lowercase() !in SEX_VALID_VALUES) {
                errors += BulkAddPersonsDataError(lineNum, HEADER_SEX, sex)
            }

            val dateOfBirthStr = row[HEADER_DATE_OF_BIRTH]
            if(!dateOfBirthStr.isNullOrBlank()) {
                try {
                    LocalDate.parse(dateOfBirthStr)
                }catch(e: Throwable) {
                    errors += BulkAddPersonsDataError(lineNum, HEADER_DATE_OF_BIRTH, dateOfBirthStr)
                }
            }

            val emailStr = row[HEADER_EMAIL]
            if(!emailStr.isNullOrBlank() && validateEmailUseCase(emailStr) == null) {
                errors += BulkAddPersonsDataError(lineNum, HEADER_EMAIL, emailStr)
            }

            val phoneNum = row[HEADER_PHONE]
            if(!phoneNum.isNullOrBlank() && !validatePhoneNumUseCase.isValid(phoneNum)) {
                errors += BulkAddPersonsDataError(lineNum, HEADER_PHONE, phoneNum)
            }

            row[HEADER_COURSES_ENROL_AS_STUDENT]?.also {
                allCourseNames += it.parseCourseNames()
            }

            row[HEADER_COURSES_ENROL_AS_TEACHER]?.also {
                allCourseNames += it.parseCourseNames()
            }
        }

        if(errors.isNotEmpty()) {
            Napier.w("ERRORS: BulkAddPersonsUseCase: ${errors.joinToString()}")
            throw BulkAddPersonException(errors = errors)
        }

        val duplicateUsernames = allUsernames.map { it.first }.duplicates()
        if(duplicateUsernames.isNotEmpty()) {
            throw IllegalArgumentException("Duplicate usernames: ${duplicateUsernames.joinToString()}")
        }

        //check for existing usernames
        val effectiveDb = activeRepo ?: activeDb
        val existingUsernames =  mutableSetOf<String>()
        allUsernames.chunked(100).forEach {
            existingUsernames += effectiveDb.personDao().selectExistingUsernames(
                it.map { it.first.lowercase() }
            ).mapNotNull { it }
        }

        if(existingUsernames.isNotEmpty()) {
            throw IllegalArgumentException("Usernames already exist: ${existingUsernames.joinToString()}")
        }

        val courseUidMap = mutableMapOf<String, Clazz>()
        val missingCourseNames = mutableSetOf<String>()
        allCourseNames.chunked(100).forEach { nameList ->
            val clazzesFound = effectiveDb.clazzDao().getCoursesByName(nameList)
            val clazzNamesFound = clazzesFound.mapNotNull { it.clazzName }.toSet()

            clazzesFound.forEach {
                courseUidMap[it.clazzName ?: ""] = it
            }

            nameList.filter { it !in clazzNamesFound }.forEach { clazzName ->
                val newClazz = Clazz(
                    clazzName = clazzName,
                ).also {
                    it.clazzStartTime = Clock.System.now()
                        .toLocalMidnight(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                    it.clazzUid = createNewClazzUseCase(it)
                }

                courseUidMap[clazzName] = newClazz
            }
        }

        if(missingCourseNames.isNotEmpty()) {
            throw IllegalArgumentException("Cannot find courses: ${missingCourseNames.joinToString()}")
        }

        val chunkSize = 10

        //Importing too many users in a single transaction will result in the transaction timing out.
        onProgress(0, csvData.size)

        csvData.chunked(chunkSize).forEachIndexed { chunkIndex, rowsChunk ->
            effectiveDb.withDoorTransactionAsync {
                rowsChunk.forEach { row ->
                    val personUid = addNewPersonUseCase(
                        person = Person().also {
                            it.username = row[HEADER_USERNAME]!!.lowercase().trim()
                            it.firstNames = row[HEADER_FIRSTNAMES]!!.trim()
                            it.lastName = row[HEADER_FAMILYNAME]!!.trim()
                            it.gender = SEX_VALID_VALUES_MAP[row[HEADER_SEX]!!.trim().lowercase()]!!
                            it.dateOfBirth = row[HEADER_DATE_OF_BIRTH]?.let { dateOfBirthStr ->
                                LocalDateTime(LocalDate.parse(dateOfBirthStr), LocalTime(0, 0))
                                    .toInstant(TimeZone.UTC).toEpochMilliseconds()
                            } ?: 0L
                            it.emailAddr = row[HEADER_EMAIL]
                            it.phoneNum = row[HEADER_PHONE]
                        }
                    )
                    authManager.setAuth(personUid, row[HEADER_PASSWORD]!!.trim())

                    processEnrolments(
                        personUid = personUid,
                        courseNameListCol = row[HEADER_COURSES_ENROL_AS_STUDENT],
                        role = ClazzEnrolment.ROLE_STUDENT,
                        courseUidMap = courseUidMap
                    )

                    processEnrolments(
                        personUid = personUid,
                        courseNameListCol = row[HEADER_COURSES_ENROL_AS_TEACHER],
                        role = ClazzEnrolment.ROLE_TEACHER,
                        courseUidMap = courseUidMap
                    )
                }

                onProgress(chunkIndex * chunkSize, csvData.size)
            }
        }

        onProgress(csvData.size, csvData.size)

        return BulkAddPersonsUseCase.BulkAddUsersResult(csvData.size)
    }

    companion object {

        const val HEADER_USERNAME = "username"

        const val HEADER_FIRSTNAMES = "givenName"

        const val HEADER_FAMILYNAME = "familyName"

        const val HEADER_SEX = "sex"

        const val HEADER_DATE_OF_BIRTH = "dateOfBirth"

        const val HEADER_ORG_ID = "identifier"

        const val HEADER_EMAIL = "email"

        const val HEADER_PHONE = "phone"

        const val HEADER_PASSWORD = "password"

        const val HEADER_COURSES_ENROL_AS_STUDENT = "courses-student-enrolments"

        const val HEADER_COURSES_ENROL_AS_TEACHER = "courses-teacher-enrolments"

        val REQUIRED_COLUMNS = listOf(
            HEADER_USERNAME, HEADER_FIRSTNAMES,
            HEADER_FAMILYNAME, HEADER_SEX, HEADER_PASSWORD
        )

        val SEX_VALID_VALUES = listOf("male", "female", "other")

        val SEX_VALID_VALUES_MAP = mapOf(
            "male" to Person.GENDER_MALE,
            "female" to Person.GENDER_FEMALE,
            "other" to Person.GENDER_OTHER
        )


    }
}