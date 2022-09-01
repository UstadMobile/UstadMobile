package com.ustadmobile.util.test.ext

import com.soywiz.klock.Date
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.initPreloadedVerbs
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.Companion.REL_TYPE_TRANSLATED_VERSION
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlin.random.Random

data class TestClazzAndMembers (val clazz: Clazz, val teacherList: List<ClazzEnrolment>, val studentList: List<ClazzEnrolment>)


private fun Person.asClazzMember(clazzUid: Long, clazzMemberRole: Int, joinTime: Long): ClazzEnrolment {
    return ClazzEnrolment(clazzUid, this.personUid, clazzMemberRole).apply {
        clazzEnrolmentDateJoined = joinTime
    }
}

fun UmAppDatabase.insertTestClazzAssignment(admin: Boolean = false, endpoint: String): ClazzAssignmentRollUp{

    val testClazz = Clazz().apply {
        clazzUid = 100
        clazzDao.insert(this)
    }

    val student = Person().apply{
        firstNames = "Student"
        lastName = "A"
        this.admin = admin
        this.personUid = 42
        personDao.insert(this)
    }

    val studentB = Person().apply {
        firstNames = "B"
        lastName = "Student"
        personUid = 12
        personDao.insert(this)
    }

    val studentC = Person().apply {
        firstNames = "C"
        lastName = "Student"
        personUid = 13
        personDao.insert(this)
    }

    val contentEntry = ContentEntry().apply {
        title = "Quiz 1"
        this.contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
        this.description = "Math Quiz"
        leaf = true
        contentEntryUid = contentEntryDao.insert(this)
    }

    val clazzEnrolment = ClazzEnrolment().apply {
        clazzEnrolmentClazzUid = testClazz.clazzUid
        clazzEnrolmentDateJoined = DateTime(2021, 5, 1).unixMillisLong
        clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT
        clazzEnrolmentPersonUid = student.personUid
        clazzEnrolmentOutcome = ClazzEnrolment.OUTCOME_IN_PROGRESS
        clazzEnrolmentUid = clazzEnrolmentDao.insert(this)
    }

    val clazzEnrolmentB = ClazzEnrolment().apply {
        clazzEnrolmentClazzUid = testClazz.clazzUid
        clazzEnrolmentDateJoined = DateTime(2021, 5, 1).unixMillisLong
        clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT
        clazzEnrolmentPersonUid = studentB.personUid
        clazzEnrolmentOutcome = ClazzEnrolment.OUTCOME_IN_PROGRESS
        clazzEnrolmentUid = clazzEnrolmentDao.insert(this)
    }

    val clazzEnrolmentC = ClazzEnrolment().apply {
        clazzEnrolmentClazzUid = testClazz.clazzUid
        clazzEnrolmentDateJoined = DateTime(2021, 5, 1).unixMillisLong
        clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT
        clazzEnrolmentPersonUid = studentC.personUid
        clazzEnrolmentOutcome = ClazzEnrolment.OUTCOME_IN_PROGRESS
        clazzEnrolmentUid = clazzEnrolmentDao.insert(this)
    }


    val clazzAssignment = ClazzAssignment().apply {
        caTitle = "New Clazz Assignment"
        caDescription = "complete quiz"
        caRequireFileSubmission = true
        caFileType = ClazzAssignment.FILE_TYPE_ANY
        caClazzUid = testClazz.clazzUid
        caUid = clazzAssignmentDao.insert(this)
    }

    val clazzAssignmentObjectId = UMFileUtil.joinPaths(endpoint,
            "/clazzAssignment/${clazzAssignment.caUid}")
    val xobject = XObjectEntity().apply {
        this.objectId = clazzAssignmentObjectId
        this.objectType = "Activity"
    }
    xobject.xObjectUid = xObjectDao.insert(xobject)
    clazzAssignment.caXObjectUid = xobject.xObjectUid
    clazzAssignmentDao.update(clazzAssignment)


    ClazzAssignmentContentJoin().apply {
        cacjContentUid = contentEntry.contentEntryUid
        cacjAssignmentUid = clazzAssignment.caUid
        cacjUid = clazzAssignmentContentJoinDao.insert(this)
    }

    val cacheClazzAssignment = ClazzAssignmentRollUp().apply {
        this.cacheClazzAssignmentUid = clazzAssignment.caUid
        this.cacheContentEntryUid = contentEntry.contentEntryUid
        this.cacheContentComplete = true
        this.cacheMaxScore = 15
        this.cachePersonUid = student.personUid
        this.cacheStudentScore = 5
        this.cacheProgress = 100
        this.cacheUid = clazzAssignmentRollUpDao.insert(this)
    }

    StatementEntity().apply {
        statementContentEntryUid = contentEntry.contentEntryUid
        contentEntryRoot = true
        resultCompletion = true
        extensionProgress = 100
        resultScoreRaw = 5
        resultScoreMax = 15
        statementPersonUid = student.personUid
        statementVerbUid = VerbEntity.VERB_COMPLETED_UID
        contextRegistration = randomUuid().toString()
        statementUid = statementDao.insert(this)
    }

    StatementEntity().apply {
        statementPersonUid = student.personUid
        statementVerbUid = VerbEntity.VERB_SUBMITTED_UID
        contextRegistration = randomUuid().toString()
        xObjectUid = xobject.xObjectUid
        statementUid = 100
        statementId = "studentA"
        statementDao.insert(this)
    }

    StatementEntity().apply {
        statementPersonUid = studentB.personUid
        statementVerbUid = VerbEntity.VERB_SUBMITTED_UID
        contextRegistration = randomUuid().toString()
        xObjectUid = xobject.xObjectUid
        statementUid = 200
        statementId = "studentB"
        statementDao.insert(this)
    }

    StatementEntity().apply {
        statementPersonUid = studentC.personUid
        statementVerbUid = VerbEntity.VERB_SUBMITTED_UID
        contextRegistration = randomUuid().toString()
        xObjectUid = xobject.xObjectUid
        statementUid = 300
        statementId = "studentC"
        statementDao.insert(this)
    }



    return cacheClazzAssignment

}


suspend fun UmAppDatabase.insertTestClazzAndMembers(numClazzStudents: Int, numClazzTeachers: Int = 1,
                                                    clazzJoinTime: Long = (getSystemTimeInMillis() - (86400 * 1000)),
                                                    studentNamer: (Int) -> Pair<String, String> = { "Test" to "Student $it" },
                                                    teacherNamer: (Int) -> Pair<String, String> = { "Test" to "Teacher $it" }): TestClazzAndMembers {
    val mockClazz = Clazz("Test Clazz").apply {
        clazzTimeZone = "Asia/Dubai"
        clazzUid = clazzDao.insertAsync(this)
    }

    val testStudents = (1..numClazzStudents).map {
        val (firstName, lastName) = studentNamer(it)
        Person("studentuser$it", firstName, lastName).apply {
            personUid = personDao.insertAsync(this)
        }
    }

    val testTeachers = (1..numClazzTeachers).map {
        val (firstName, lastName) = teacherNamer(it)
        Person("studentuser$it", firstName, lastName).apply {
            personUid = personDao.insertAsync(this)
        }
    }

    val testStudentClazzMembers = testStudents.map {
        it.asClazzMember(mockClazz.clazzUid, ClazzEnrolment.ROLE_STUDENT, clazzJoinTime).apply {
            clazzEnrolmentUid = clazzEnrolmentDao.insertAsync(this)
        }
    }

    val testTeacherClazzMembers = testTeachers.map {
        it.asClazzMember(mockClazz.clazzUid, ClazzEnrolment.ROLE_TEACHER, clazzJoinTime).apply {
            clazzEnrolmentUid = clazzEnrolmentDao.insertAsync(this)
        }
    }

    return TestClazzAndMembers(mockClazz, testTeacherClazzMembers, testStudentClazzMembers)
}

suspend fun UmAppDatabase.insertClazzLogs(clazzUid: Long, numLogs: Int, logMaker: (Int) -> ClazzLog): List<ClazzLog> {
    return (0 until numLogs).map { index ->
        logMaker(index).apply {
            clazzLogClazzUid = clazzUid
            clazzLogUid = clazzLogDao.insertAsync(this)
        }
    }
}

suspend fun UmAppDatabase.insertContentEntryWithTranslations(numTranslations: Int, entryUid: Long): ContentEntry {
    val entry = ContentEntry().apply {
        title = "Dummy Content Entry"
        leaf = true
        description = "Dummy Entry description"
        contentEntryUid = entryUid
        contentEntryDao.insertAsync(this)
    }

    (1..numTranslations).map {
        val entryOfLanguage = ContentEntry().apply {
            title = "Language $it Content Entry"
            leaf = true
            description = "Dummy Entry description language $it"
            contentEntryUid = contentEntryDao.insertAsync(this)
        }
        val language = Language().apply {
            name = "Language $it"
            iso_639_2_standard = "${if (it >= 10) it else "0$it"}"
            langUid = languageDao.insertAsync(this)
        }

        ContentEntryRelatedEntryJoin().apply {
            cerejContentEntryUid = entry.contentEntryUid
            cerejRelatedEntryUid = entryOfLanguage.contentEntryUid
            cerejRelLanguageUid = language.langUid
            relType = REL_TYPE_TRANSLATED_VERSION
            cerejUid = contentEntryRelatedEntryJoinDao.insertAsync(this)
        }
    }
    return entry
}

suspend fun UmAppDatabase.insertContentEntryWithParentChildJoinAndMostRecentContainer(
        numEntries: Int, parentEntryUid: Long, nonLeafIndexes: MutableList<Int> = mutableListOf()): List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> {
    return (1 .. numEntries).map {
        val entry = ContentEntry().apply {
            leaf = !(nonLeafIndexes.isNotEmpty() && nonLeafIndexes.indexOf(it - 1) != -1)
            title = "Dummy ${if(leaf) " entry" else "folder"} title $it"
            description = "Dummy description $it"
            contentEntryUid = contentEntryDao.insertAsync(this)
        }
        val parentChildJoin = ContentEntryParentChildJoin().apply {
            cepcjChildContentEntryUid = entry.contentEntryUid
            cepcjParentContentEntryUid = parentEntryUid
            cepcjUid = contentEntryParentChildJoinDao.insertAsync(this)
        }

        val container = Container().apply {
            fileSize = 10000
            cntLastModified = getSystemTimeInMillis()
            containerContentEntryUid = entry.contentEntryUid
            containerUid = containerDao.insertAsync(this)
        }



        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
            mostRecentContainer = container
            contentEntryParentChildJoin = parentChildJoin
            contentEntryUid = entry.contentEntryUid
            leaf = entry.leaf
            title = entry.title
            description = entry.description
            contentEntryUid = entry.contentEntryUid
        }
    }
}


suspend fun UmAppDatabase.insertStatementForSessions(){

    val entry = ContentEntry().apply {
        title = "English Quiz"
        leaf = true
        contentEntryUid = 1000
        entryId = "Quiz"
        contentEntryDao.insert(this)
    }

    verbDao.initPreloadedVerbs()

    val completedLangMap = XLangMapEntry(VerbEntity.VERB_COMPLETED_UID, 0,
            0, 0, "Completed")
    completedLangMap.languageLangMapUid = xLangMapEntryDao.insert(completedLangMap)

    val progressLangMap = XLangMapEntry(VerbEntity.VERB_PROGRESSED_UID, 0,
            0, 0, "Progressed")
    progressLangMap.languageLangMapUid = xLangMapEntryDao.insert(progressLangMap)

    val firstObject = XObjectEntity().apply {
        objectId = "hello"
        objectContentEntryUid = entry.contentEntryUid
        xObjectUid =  xObjectDao.insert(this)
    }

    val objectLangMap = XLangMapEntry(0, firstObject.xObjectUid,
            0, 0, "Quiz 1")
    objectLangMap.languageLangMapUid = xLangMapEntryDao.insert(objectLangMap)



    val personJohn = Person().apply {
        firstNames = "John"
        lastName = "Doe"
        personUid = 1000
        personDao.insert(this)
    }

    val personJane = Person().apply {
        firstNames = "Jane"
        lastName = "Teacher"
        admin = true
        personUid = personDao.insert(this)
    }



    val session1 =  randomUuid().toString()
    val sessionToTest = "abc"

    StatementEntity().apply {
        statementPersonUid = personJohn.personUid
        resultDuration = 2400000
        resultCompletion = false
        resultScoreScaled = 0.4f
        resultScoreMax = 5
        resultScoreRaw = 2
        resultScoreMin = 0
        contextRegistration = session1
        statementVerbUid = VerbEntity.VERB_COMPLETED_UID
        xObjectUid = firstObject.xObjectUid
        statementContentEntryUid = entry.contentEntryUid
        resultSuccess = StatementEntity.RESULT_FAILURE
        timestamp = DateTime(2019, 6, 11, 23, 30, 0).unixMillisLong
        extensionProgress = 100
        statementId = randomUuid().toString()
        contentEntryRoot = true
        statementUid = statementDao.insert(this)
    }

    StatementEntity().apply {
        statementPersonUid = personJohn.personUid
        resultDuration = 2060090
        resultCompletion = true
        resultScoreScaled = 1f
        resultScoreMax = 5
        resultScoreRaw = 5
        resultScoreMin = 0
        contextRegistration = sessionToTest
        statementVerbUid = VerbEntity.VERB_COMPLETED_UID
        xObjectUid = firstObject.xObjectUid
        statementContentEntryUid = entry.contentEntryUid
        resultSuccess = StatementEntity.RESULT_SUCCESS
        timestamp = DateTime(2019, 6, 12, 20, 30, 0).unixMillisLong
        extensionProgress = 100
        statementId = randomUuid().toString()
        contentEntryRoot = true
        statementUid = statementDao.insert(this)
    }

    for(i in 0..9){
        StatementEntity().apply {
            statementPersonUid = personJohn.personUid
            resultDuration = Random.nextLong(23000, 180000)
            resultCompletion = false
            contextRegistration = sessionToTest
            statementVerbUid = VerbEntity.VERB_PROGRESSED_UID
            xObjectUid = firstObject.xObjectUid
            statementContentEntryUid = entry.contentEntryUid
            statementId = randomUuid().toString()
            resultSuccess = StatementEntity.RESULT_SUCCESS
            extensionProgress = 1 * 10
            timestamp = DateTime(2019, 6, 11).unixMillisLong
            statementUid = statementDao.insert(this)
        }


        StatementEntity().apply {
            statementPersonUid = personJane.personUid
            resultDuration = Random.nextLong(23000, 180000)
            resultCompletion = false
            contextRegistration =  if(Random.nextBoolean()) session1 else sessionToTest
            statementVerbUid = VerbEntity.VERB_PROGRESSED_UID
            xObjectUid = firstObject.xObjectUid
            statementContentEntryUid = entry.contentEntryUid
            statementId = randomUuid().toString()
            resultSuccess = StatementEntity.RESULT_SUCCESS
            extensionProgress = 1 * 10
            timestamp = DateTime(2019, 6, 11).unixMillisLong
            statementUid = statementDao.insert(this)
        }
    }



}




fun UmAppDatabase.insertTestStatementsForReports() {

    val firstPerson = Person().apply {
        firstNames = "Bobb"
        lastName = "Ross"
        gender = Person.GENDER_MALE
        dateOfBirth = Date(1995, 10,10).dateTimeDayStart.unixMillisLong
        personUid = personDao.insert(this)
    }

    val secondPerson = Person().apply {
        firstNames = "Calvin"
        lastName = "Neat"
        gender = Person.GENDER_MALE
        dateOfBirth = Date(2005, 12, 1).dateTimeDayStart.unixMillisLong
        personUid = personDao.insert(this)
    }

    val thirdPerson = Person().apply {
        firstNames = "Jane"
        lastName = "Doe"
        gender = Person.GENDER_FEMALE
        dateOfBirth = Date(1992, 4,12).dateTimeDayStart.unixMillisLong
        personUid = personDao.insert(this)
    }

    val fourthPerson = Person().apply {
        firstNames = "Jen"
        lastName = "Walts"
        gender = Person.GENDER_FEMALE
        dateOfBirth = Date(1993, 5,15).dateTimeDayStart.unixMillisLong
        personUid = personDao.insert(this)
    }

    val arabicClazz = Clazz().apply {
        clazzName = "Arabic"
        clazzUid = 200
        clazzDao.insert(this)
    }

    val scienceClazz = Clazz().apply {
        clazzName = "science"
        clazzUid = 400
        clazzDao.insert(this)
    }

     ClazzEnrolment().apply {
        clazzEnrolmentPersonUid = firstPerson.personUid
        clazzEnrolmentClazzUid = arabicClazz.clazzUid
         clazzEnrolmentUid = clazzEnrolmentDao.insert(this)
    }

    ClazzEnrolment().apply {
        clazzEnrolmentPersonUid = firstPerson.personUid
        clazzEnrolmentClazzUid = scienceClazz.clazzUid
        clazzEnrolmentUid = clazzEnrolmentDao.insert(this)
    }

    ClazzEnrolment().apply {
        clazzEnrolmentPersonUid = secondPerson.personUid
        clazzEnrolmentClazzUid = arabicClazz.clazzUid
        clazzEnrolmentUid = clazzEnrolmentDao.insert(this)
    }

    ClazzEnrolment().apply {
        clazzEnrolmentPersonUid = thirdPerson.personUid
        clazzEnrolmentClazzUid = arabicClazz.clazzUid
        clazzEnrolmentUid = clazzEnrolmentDao.insert(this)
    }

    ClazzEnrolment().apply {
        clazzEnrolmentPersonUid = fourthPerson.personUid
        clazzEnrolmentClazzUid = scienceClazz.clazzUid
        clazzEnrolmentUid = clazzEnrolmentDao.insert(this)
    }


    val completedVerb = VerbEntity().apply {
        urlId = VerbEntity.VERB_COMPLETED_URL
        verbUid = VerbEntity.VERB_COMPLETED_UID
        verbDao.insert(this)
    }

    val firstVerbLangMap = XLangMapEntry(completedVerb.verbUid, 0, 0, 0, "completed Entry 1")
    firstVerbLangMap.languageLangMapUid = xLangMapEntryDao.insert(firstVerbLangMap)

    val passedVerb = VerbEntity().apply {
        urlId = VerbEntity.VERB_PASSED_URL
        verbUid = VerbEntity.VERB_PASSED_UID
        verbDao.insert(this)
    }

    val secondVerbLangMap = XLangMapEntry(passedVerb.verbUid, 0, 0, 0, "Passed Entry 1")
    secondVerbLangMap.languageLangMapUid = xLangMapEntryDao.insert(secondVerbLangMap)

    val failedVerb = VerbEntity().apply {
        urlId = VerbEntity.VERB_FAILED_URL
        verbUid = VerbEntity.VERB_FAILED_UID
        verbDao.insert(this)
    }

    val thirdVerbLangMap = XLangMapEntry(failedVerb.verbUid, 0, 0, 0, "Failed Entry 1")
    thirdVerbLangMap.languageLangMapUid = xLangMapEntryDao.insert(thirdVerbLangMap)

    val firstEntry = ContentEntry()
    firstEntry.title = "Ustad Mobile"
    firstEntry.contentEntryUid =  532
   contentEntryDao.insert(firstEntry)

    var secondEntry = ContentEntry()
    secondEntry.title = "Khan Academy"
    secondEntry.contentEntryUid = 530
    contentEntryDao.insert(secondEntry)

    ContentEntryParentChildJoin().apply {
        cepcjParentContentEntryUid = -4103245208651563007L
        cepcjChildContentEntryUid = firstEntry.contentEntryUid
        contentEntryParentChildJoinDao.insert(this)
    }

    var firstsecondJoin = ContentEntryParentChildJoin()
    firstsecondJoin.cepcjParentContentEntryUid = firstEntry.contentEntryUid
    firstsecondJoin.cepcjChildContentEntryUid = secondEntry.contentEntryUid
    firstsecondJoin.cepcjUid = contentEntryParentChildJoinDao.insert(firstsecondJoin)

    var phetEntry = ContentEntry()
    phetEntry.title = "PHET"
    phetEntry.contentEntryUid = 232
    contentEntryDao.insert(phetEntry)

    var phetJoin = ContentEntryParentChildJoin()
    phetJoin.cepcjParentContentEntryUid = firstEntry.contentEntryUid
    phetJoin.cepcjChildContentEntryUid = phetEntry.contentEntryUid
    phetJoin.cepcjUid = contentEntryParentChildJoinDao.insert(phetJoin)

    var edraakEntry = ContentEntry()
    edraakEntry.title = "EDRAAK"
    edraakEntry.contentEntryUid = 3423
    contentEntryDao.insert(edraakEntry)

    var edraakJoin = ContentEntryParentChildJoin()
    edraakJoin.cepcjParentContentEntryUid = firstEntry.contentEntryUid
    edraakJoin.cepcjChildContentEntryUid = edraakEntry.contentEntryUid
    edraakJoin.cepcjUid = contentEntryParentChildJoinDao.insert(edraakJoin)

    var khanclass1 = ContentEntry()
    khanclass1.title = "Content 1"
    khanclass1.entryId = "hello"
    khanclass1.contentEntryUid = 23223
    khanclass1.leaf = true
    contentEntryDao.insert(khanclass1)

    ContentEntryParentChildJoin().apply {
        cepcjParentContentEntryUid = -4103245208651563007L
        cepcjChildContentEntryUid = secondEntry.contentEntryUid
        contentEntryParentChildJoinDao.insert(this)
    }

    var khanclassJoin = ContentEntryParentChildJoin()
    khanclassJoin.cepcjParentContentEntryUid = secondEntry.contentEntryUid
    khanclassJoin.cepcjChildContentEntryUid = khanclass1.contentEntryUid
    khanclassJoin.cepcjUid = contentEntryParentChildJoinDao.insert(khanclassJoin)

    var khanclass2 = ContentEntry()
    khanclass2.title = "Content 2"
    khanclass2.entryId = "world"
    khanclass2.contentEntryUid = 2422
    khanclass2.leaf = true
    contentEntryDao.insert(khanclass2)

    var khanclass2Join = ContentEntryParentChildJoin()
    khanclass2Join.cepcjParentContentEntryUid = secondEntry.contentEntryUid
    khanclass2Join.cepcjChildContentEntryUid = khanclass2.contentEntryUid
    khanclass2Join.cepcjUid = contentEntryParentChildJoinDao.insert(khanclass2Join)

    var firstObject = XObjectEntity()
    firstObject.objectId = "hello"
    firstObject.objectContentEntryUid = khanclass1.contentEntryUid
    firstObject.xObjectUid =  xObjectDao.insert(firstObject)


    var firstObjectLangMap = XLangMapEntry(0, firstObject.xObjectUid, 0, 0, khanclass1.title!!)
    firstObjectLangMap.languageLangMapUid = xLangMapEntryDao.insert(firstObjectLangMap)

    var secondObject = XObjectEntity()
    secondObject.objectId = "world"
    secondObject.objectContentEntryUid = khanclass2.contentEntryUid
    secondObject.xObjectUid =  xObjectDao.insert(secondObject)


    var secondObjectLangMap = XLangMapEntry(0, secondObject.xObjectUid, 0, 0, khanclass2.title!!)
    secondObjectLangMap.languageLangMapUid = xLangMapEntryDao.insert(secondObjectLangMap)

    var thirdObject = XObjectEntity()
    thirdObject.objectId = "now"
    thirdObject.objectContentEntryUid = khanclass1.contentEntryUid
    thirdObject.xObjectUid =  xObjectDao.insert(thirdObject)


    var thirdObjectLangMap = XLangMapEntry(0, thirdObject.xObjectUid, 0, 0, khanclass1.title!!)
    thirdObjectLangMap.languageLangMapUid = xLangMapEntryDao.insert(thirdObjectLangMap)
    
    StatementEntity().apply {
        statementPersonUid = firstPerson.personUid
        resultDuration = 2400000
        resultCompletion = true
        resultScoreScaled = 50f
        contextRegistration = randomUuid().toString()
        statementVerbUid = completedVerb.verbUid
        xObjectUid = firstObject.xObjectUid
        statementContentEntryUid = khanclass1.contentEntryUid
        resultSuccess = StatementEntity.RESULT_FAILURE
        timestamp = DateTime(2019, 6, 11).unixMillisLong
        statementId = randomUuid().toString()
        contentEntryRoot = true
        statementUid = statementDao.insert(this)
    }
  
    StatementEntity().apply {
        statementPersonUid = firstPerson.personUid
        resultDuration = 7200000
        resultScoreScaled = 100f
        resultCompletion = true
        contextRegistration =randomUuid().toString()
        statementVerbUid = passedVerb.verbUid
        xObjectUid = firstObject.xObjectUid
        statementContentEntryUid = khanclass1.contentEntryUid
        resultSuccess = StatementEntity.RESULT_SUCCESS
        statementId = randomUuid().toString()
        timestamp = DateTime(2019, 5, 1).unixMillisLong
        contentEntryRoot = false
        statementUid = statementDao.insert(this)
    }

    val commonSessionForSecondPerson =randomUuid().toString()

    StatementEntity().apply {
        statementPersonUid = secondPerson.personUid
        resultDuration = 600000
        resultScoreScaled = 50f
        resultCompletion = true
        contextRegistration = commonSessionForSecondPerson
        statementVerbUid = completedVerb.verbUid
        xObjectUid = secondObject.xObjectUid
        statementContentEntryUid = khanclass2.contentEntryUid
        resultSuccess = StatementEntity.RESULT_FAILURE
        statementId = randomUuid().toString()
        timestamp = DateTime(2019, 4, 10).unixMillisLong
        contentEntryRoot = true
        statementUid = statementDao.insert(this)
    }
    

    val commonSession =randomUuid().toString()
    StatementEntity().apply {
        statementPersonUid = thirdPerson.personUid
        resultDuration = 120000
        resultScoreScaled = 20f
        resultCompletion = true
        contextRegistration = commonSession
        statementVerbUid = completedVerb.verbUid
        xObjectUid = secondObject.xObjectUid
        statementContentEntryUid = khanclass2.contentEntryUid
        statementId = randomUuid().toString()
        resultSuccess = StatementEntity.RESULT_SUCCESS
        timestamp = DateTime(2019, 6, 30).unixMillisLong
        contentEntryRoot = true
        statementUid = statementDao.insert(this)
    }
    


    StatementEntity().apply {
        statementPersonUid = fourthPerson.personUid
        resultDuration = 100000
        resultScoreScaled = 85f
        resultCompletion = true
        contextRegistration = randomUuid().toString()
        statementVerbUid = failedVerb.verbUid
        xObjectUid = firstObject.xObjectUid
        statementContentEntryUid = khanclass1.contentEntryUid
        statementId = randomUuid().toString()
        resultSuccess = StatementEntity.RESULT_SUCCESS
        timestamp = DateTime(2019, 7, 10).unixMillisLong
        contentEntryRoot = true
        statementUid = statementDao.insert(this)
    }
  


    StatementEntity().apply {
        statementPersonUid = thirdPerson.personUid
        resultDuration = 60000
        resultScoreScaled = 25f
        resultCompletion = true
        contextRegistration = commonSession
        statementVerbUid = completedVerb.verbUid
        statementId = randomUuid().toString()
        resultSuccess = StatementEntity.RESULT_FAILURE
        xObjectUid = secondObject.xObjectUid
        statementContentEntryUid = khanclass2.contentEntryUid
        timestamp = DateTime(2019, 5, 25).unixMillisLong
        contentEntryRoot = true
        statementUid = statementDao.insert(this)
    }


    StatementEntity().apply {
        statementPersonUid = secondPerson.personUid
        resultDuration = 30000
        resultScoreScaled = 5f
        resultCompletion = true
        contextRegistration = commonSessionForSecondPerson
        statementVerbUid = completedVerb.verbUid
        xObjectUid = firstObject.xObjectUid
        statementContentEntryUid = khanclass1.contentEntryUid
        statementId = randomUuid().toString()
        resultSuccess = StatementEntity.RESULT_FAILURE
        timestamp = DateTime(2019, 6, 11).unixMillisLong
        contentEntryRoot = true
        statementUid = statementDao.insert(this)
    }

    for(i in 0..10){
        StatementEntity().apply {
            statementPersonUid = secondPerson.personUid
            resultDuration = 30000
            resultScoreScaled = 5f
            resultCompletion = false
            contextRegistration = randomUuid().toString()
            statementVerbUid = completedVerb.verbUid
            xObjectUid = thirdObject.xObjectUid
            statementContentEntryUid = khanclass1.contentEntryUid
            statementId =randomUuid().toString()
            resultSuccess = StatementEntity.RESULT_SUCCESS
            timestamp = DateTime(2019, 6, 11).unixMillisLong
            statementUid = statementDao.insert(this)
        }
    }
}

suspend fun UmAppDatabase.insertVideoContent(): Container {
    val spanishQuiz = ContentEntry()
    spanishQuiz.title = "tiempo de prueba"
    spanishQuiz.thumbnailUrl = "https://www.africanstorybook.org/img/asb120.png"
    spanishQuiz.description = "todo el contenido"
    spanishQuiz.publisher = "CK12"
    spanishQuiz.author = "borrachera"
    spanishQuiz.primaryLanguageUid = 3
    spanishQuiz.leaf = true
    spanishQuiz.contentEntryUid = contentEntryDao.insert(spanishQuiz)

    return Container().apply {
        containerContentEntryUid = spanishQuiz.contentEntryUid
        containerUid  = containerDao.insert(this)
    }
}

