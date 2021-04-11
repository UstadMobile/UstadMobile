package com.ustadmobile.util.test.ext

import com.soywiz.klock.Date
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.Companion.REL_TYPE_TRANSLATED_VERSION
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlin.random.Random

data class TestClazzAndMembers (val clazz: Clazz, val teacherList: List<ClazzEnrolment>, val studentList: List<ClazzEnrolment>)
data class TestClazzWork(val clazzAndMembers: TestClazzAndMembers, val clazzWork: ClazzWork,
            val quizQuestionsAndOptions: TestClazzWorkWithQuestionAndOptionsAndResponse? = null,
                         val submissions: List<ClazzWorkSubmission>? = mutableListOf())
data class TestClazzWorkWithQuestionAndOptionsAndResponse(val clazzWork: ClazzWork,
      val questionsAndOptions: List<ClazzWorkQuestionAndOptions>, val responses: List<ClazzWorkQuestionResponse?>)
data class TestContentAndJoin(val contentList : List<ContentEntry> ,
                               val joinList: List<ClazzWorkContentJoin>)


private fun Person.asClazzMember(clazzUid: Long, clazzMemberRole: Int, joinTime: Long): ClazzEnrolment {
    return ClazzEnrolment(clazzUid, this.personUid, clazzMemberRole).apply {
        clazzEnrolmentDateJoined = joinTime
    }
}

suspend fun UmAppDatabase.insertTestClazzWork(clazzWork: ClazzWork):TestClazzWork {
    val clazzAndMembers = insertTestClazzAndMembers(5, 2)
    clazzWork.apply{
        clazzWorkClazzUid = clazzAndMembers.clazz.clazzUid
        clazzWorkUid = clazzWorkDao.insertAsync(this)
    }

    return TestClazzWork(clazzAndMembers, clazzWork)
}

suspend fun UmAppDatabase.insertPublicAndPrivateComments(dateNow: Long, clazzWork:
                ClazzWork, clazzAndMembers: TestClazzAndMembers){
    //Public+Private comments
    for(studentWithIndex in clazzAndMembers.studentList.withIndex()){
        val index = studentWithIndex.index
        val student = studentWithIndex.value
        Comments().apply {
            commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
            commentsEntityUid = clazzWork.clazzWorkUid
            commentsPublic = index%2 ==0
            if(commentsPublic){
                commentsText = "Public comment $index"
            }else{
                commentsText = "Private comment $index"
            }
            commentsPersonUid = student.clazzEnrolmentPersonUid
            commentsInActive = false
            commentsDateTimeAdded = dateNow
            commentsUid = commentsDao.insertAsync(this)

        }
    }

}

suspend fun UmAppDatabase.insertQuizQuestionsAndOptions(
        clazzWork: ClazzWork, responded: Boolean = false, clazzMemberUid: Long = 0, personUid: Long = 0,
        quizQuestionType: Int, quizQuestionTypeMixed: Boolean = false,
        partialFilled: Boolean = false, clazzMember2Uid: Long = 0, person2Uid: Long = 0): TestClazzWorkWithQuestionAndOptionsAndResponse {

    clazzWork.apply {
        clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ
        if(clazzWorkUid != 0L){
            clazzWorkDao.updateAsync(this)
        }else {
            clazzWorkUid = clazzWorkDao.insertAsync(this)
        }
    }

    var  clazzWorkQuestionsAndOptions = listOf<ClazzWorkQuestionAndOptions>()
    val responses = mutableListOf<ClazzWorkQuestionResponse>()
    if(clazzWork.clazzWorkSubmissionType == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ) {
        //Create questions
        val questionNamer: (Int) -> String = { "Question $it" }
        var qn = 0
        clazzWorkQuestionsAndOptions = (1..5).map {
            qn++
            ClazzWorkQuestionAndOptions(ClazzWorkQuestion(), mutableListOf(), mutableListOf())
                    .apply {
                        clazzWorkQuestion.clazzWorkQuestionActive = true
                        clazzWorkQuestion.clazzWorkQuestionText = questionNamer(it)
                        clazzWorkQuestion.clazzWorkQuestionClazzWorkUid = clazzWork.clazzWorkUid
                        clazzWorkQuestion.clazzWorkQuestionIndex = it
                        if (!quizQuestionTypeMixed) {
                            clazzWorkQuestion.clazzWorkQuestionType = quizQuestionType
                        } else {
                            clazzWorkQuestion.clazzWorkQuestionType = if (it % 2 == 0) {
                                ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_FREE_TEXT
                            } else {
                                ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_MULTIPLE_CHOICE
                            }
                        }
                        clazzWorkQuestion.clazzWorkQuestionUid = clazzWorkQuestionDao.insertAsync(clazzWorkQuestion)
                        if (clazzWorkQuestion.clazzWorkQuestionType == ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_MULTIPLE_CHOICE) {
                            //Add options
                            val optionsToPut = (1..3).map {
                                ClazzWorkQuestionOption().apply {
                                    clazzWorkQuestionOptionText = "Question $qn Option $it"
                                    clazzWorkQuestionOptionQuestionUid = clazzWorkQuestion.clazzWorkQuestionUid
                                    clazzWorkQuestionOptionActive = true

                                    clazzWorkQuestionOptionUid = clazzWorkQuestionOptionDao.insertAsync(this)
                                }
                            }
                            options = optionsToPut
                        }else if(clazzWorkQuestion.clazzWorkQuestionType ==
                                ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_FREE_TEXT){

                        }
                    }
        }


        if (responded && clazzMemberUid != 0L && personUid != 0L ) {
            //Create question response
            for ((index, question) in clazzWorkQuestionsAndOptions.withIndex()) {

                //Skip some
                if(index == 2 && partialFilled){
                    continue
                }
                if(index == 3 && partialFilled){
                    continue
                }
                val response =
                        ClazzWorkQuestionResponse().apply {
                            clazzWorkQuestionResponseClazzWorkUid = question.clazzWorkQuestion.clazzWorkQuestionClazzWorkUid
                            clazzWorkQuestionResponseQuestionUid = question.clazzWorkQuestion.clazzWorkQuestionUid
                            if (question.clazzWorkQuestion.clazzWorkQuestionType == ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_FREE_TEXT) {
                                clazzWorkQuestionResponseText = "Answer $index"
                            } else {
                                val size = question.options.size
                                if (size > 0) {
                                    val r = Random.nextInt(0, size)
                                    clazzWorkQuestionResponseOptionSelected =
                                            question.options.get(r).clazzWorkQuestionOptionUid
                                }
                            }
                            clazzWorkQuestionResponsePersonUid = personUid
                            clazzWorkQuestionResponseInactive = false
                            //TODO: Dates
                            clazzWorkQuestionResponseDateResponded = 0

                            clazzWorkQuestionResponseUid = clazzWorkQuestionResponseDao.insertAsync(this)
                        }
                responses.add(response)


                if(clazzMember2Uid != 0L && person2Uid != 0L ) {
                    val response2 =
                            ClazzWorkQuestionResponse().apply {
                                clazzWorkQuestionResponseClazzWorkUid = question.clazzWorkQuestion.clazzWorkQuestionClazzWorkUid
                                clazzWorkQuestionResponseQuestionUid = question.clazzWorkQuestion.clazzWorkQuestionUid
                                if (question.clazzWorkQuestion.clazzWorkQuestionType == ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_FREE_TEXT) {
                                    clazzWorkQuestionResponseText = "Answer for 2nd student $index"
                                } else {
                                    val size = question.options.size
                                    if (size > 0) {
                                        val r = Random.nextInt(0, size)
                                        clazzWorkQuestionResponseOptionSelected =
                                                question.options.get(r).clazzWorkQuestionOptionUid
                                    }
                                }
                                clazzWorkQuestionResponsePersonUid = person2Uid
                                clazzWorkQuestionResponseInactive = false
                                //TODO: Dates
                                clazzWorkQuestionResponseDateResponded = 0

                                clazzWorkQuestionResponseUid = clazzWorkQuestionResponseDao.insertAsync(this)
                            }

                    responses.add(response2)
                }
            }
        }
    }

    return TestClazzWorkWithQuestionAndOptionsAndResponse(clazzWork, clazzWorkQuestionsAndOptions,
            responses)

}

suspend fun UmAppDatabase.createTestContentEntriesAndJoinToClazzWork(clazzWork: ClazzWork,
                                 numContentEntries: Int): TestContentAndJoin{

    val joinList = mutableListOf<ClazzWorkContentJoin>()
    val contentList = (1 .. numContentEntries).map {
        ContentEntry().apply {
            title = "Content  $it"
            description = "Content description $it"
            entryId = "42$it"
            author = "Mr.Tester McTestface"
            publik = true
            publisher = "TestCorp"
            leaf = true
            contentEntryUid = contentEntryDao.insertAsync(this)

            joinList.add(ClazzWorkContentJoin().apply {

                clazzWorkContentJoinContentUid = contentEntryUid
                clazzWorkContentJoinClazzWorkUid = clazzWork.clazzWorkUid
                clazzWorkContentJoinInactive = false
                clazzWorkContentJoinUid = clazzWorkContentJoinDao.insertAsync(this)
            })
        }
    }

    return TestContentAndJoin(contentList, joinList)
}

suspend fun UmAppDatabase.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
        clazzWork: ClazzWork, responded : Boolean = false, submissionType: Int = -1 ,
        quizQuestionTypeMixed: Boolean = false, quizQuestionType: Int = 0,
        submitted: Boolean = false, isStudentToClazz : Boolean = false, dateNow: Long = 0,
        marked: Boolean = true, partialFilled: Boolean = false, multipleSubmissions:Boolean = false
    ):TestClazzWork {
    val clazzAndMembers = insertTestClazzAndMembers(5, 2)
    clazzWork.apply{
        clazzWorkTitle = "Espresso Clazz Work A"
        clazzWorkClazzUid = clazzAndMembers.clazz.clazzUid
        if(submissionType < 0){
            clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ
        }else{
            clazzWorkSubmissionType = submissionType
        }
        clazzWorkMaximumScore = 120
        if(clazzWorkUid != 0L){
            clazzWorkDao.updateAsync(this)
        }else {
            clazzWorkUid = clazzWorkDao.insertAsync(this)
        }

        clazzWorkInstructions = "Pass espresso test for ClazzWork"
        clazzWorkCommentsEnabled = true
    }

    //Getting member
    val clazzEnrolment: ClazzEnrolment

    val studentClazzMember = clazzAndMembers.studentList.get(1)
    val student2ClazzMember = clazzAndMembers.studentList.get(3)
    val teacherClazzMember = clazzAndMembers.teacherList.get(0)

    if(isStudentToClazz){
        clazzEnrolment = clazzAndMembers.studentList.get(0)
    }else{
        clazzEnrolment = clazzAndMembers.teacherList.get(0)
    }

    var quizQuestionsAndOptions: TestClazzWorkWithQuestionAndOptionsAndResponse? = null
    if(clazzWork.clazzWorkSubmissionType == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ) {
        if(multipleSubmissions){
            quizQuestionsAndOptions = insertQuizQuestionsAndOptions(clazzWork, responded, studentClazzMember.clazzEnrolmentUid,
                    studentClazzMember.clazzEnrolmentPersonUid, quizQuestionType, quizQuestionTypeMixed, partialFilled,
            student2ClazzMember.clazzEnrolmentUid, student2ClazzMember.clazzEnrolmentPersonUid)
        }else {
            quizQuestionsAndOptions = insertQuizQuestionsAndOptions(clazzWork, responded, studentClazzMember.clazzEnrolmentUid,
                    studentClazzMember.clazzEnrolmentPersonUid, quizQuestionType, quizQuestionTypeMixed, partialFilled)

        }

    }

    val submissions : MutableList<ClazzWorkSubmission> = mutableListOf()
    //Create Submission
    if(submitted ){
        ClazzWorkSubmission().apply {
            clazzWorkSubmissionClazzWorkUid = clazzWork.clazzWorkUid
            clazzWorkSubmissionPersonUid = studentClazzMember.clazzEnrolmentPersonUid
            if(marked) {
                clazzWorkSubmissionMarkerPersonUid = teacherClazzMember.clazzEnrolmentPersonUid
                clazzWorkSubmissionScore = 89
                clazzWorkSubmissionDateTimeMarked = dateNow
            }
            clazzWorkSubmissionInactive = false
            clazzWorkSubmissionDateTimeStarted = dateNow - 7000
            clazzWorkSubmissionDateTimeUpdated = dateNow - 7000
            clazzWorkSubmissionDateTimeFinished = dateNow - 7000
            if(submissionType == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT){
                clazzWorkSubmissionText = "This is the test submission"
            }

            clazzWorkSubmissionUid = clazzWorkSubmissionDao.insertAsync(this)
            submissions.add(this)
        }

        if(multipleSubmissions){
            ClazzWorkSubmission().apply {
                clazzWorkSubmissionClazzWorkUid = clazzWork.clazzWorkUid
                clazzWorkSubmissionPersonUid = student2ClazzMember.clazzEnrolmentPersonUid
                if(marked) {
                    clazzWorkSubmissionMarkerPersonUid = teacherClazzMember.clazzEnrolmentPersonUid
                    clazzWorkSubmissionDateTimeMarked = dateNow
                }
                clazzWorkSubmissionInactive = false
                clazzWorkSubmissionDateTimeStarted = dateNow - 7000
                clazzWorkSubmissionDateTimeUpdated = dateNow - 7000
                clazzWorkSubmissionDateTimeFinished = dateNow - 7000
                if(submissionType == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT){
                    clazzWorkSubmissionText = "This is the test submission2"
                }

                clazzWorkSubmissionUid = clazzWorkSubmissionDao.insertAsync(this)
                submissions.add(this)
            }
        }
    }


    return TestClazzWork(clazzAndMembers, clazzWork, quizQuestionsAndOptions, submissions.toList())
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


suspend fun UmAppDatabase.insertTestStatements() {

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
        contextRegistration = randomUuid().toString()
        statementVerbUid = passedVerb.verbUid
        xObjectUid = firstObject.xObjectUid
        statementContentEntryUid = khanclass1.contentEntryUid
        resultSuccess = StatementEntity.RESULT_SUCCESS
        statementId = randomUuid().toString()
        timestamp = DateTime(2019, 5, 1).unixMillisLong
        contentEntryRoot = false
        statementUid = statementDao.insert(this)
    }

    val commonSessionForSecondPerson = randomUuid().toString()

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
    

    val commonSession = randomUuid().toString()
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
            statementId = randomUuid().toString()
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

    val container = Container()
    container.containerContentEntryUid = spanishQuiz.contentEntryUid
    val containerUid = containerDao.insert(container)
    container.containerUid = containerUid

    return container
}

suspend fun UmAppDatabase.insertPersonWithRole(person: Person, role: Role,
    entityRole: EntityRole = EntityRole()) {
    person.also {
        if(it.personUid == 0L) {

            val groupPerson = PersonGroup().apply {
                groupName = "Person individual group"
                personGroupFlag = PersonGroup.PERSONGROUP_FLAG_PERSONGROUP
            }
            //Create person's group
            groupPerson.groupUid = personGroupDao.insertAsync(groupPerson)

            //Assign to person
            it.personGroupUid = groupPerson.groupUid
            it.personUid = personDao.insertAsync(it)

            //Assign person to PersonGroup ie: Create PersonGroupMember
            personGroupMemberDao.insertAsync(
                    PersonGroupMember(it.personUid, it.personGroupUid))


        }else {
            personDao.insertOrReplace(it)
        }
    }

    role.also {
        if(it.roleUid == 0L) {
            it.roleUid = roleDao.insert(it)
        }else {
            roleDao.insertOrReplace(it)
        }
    }

    entityRole.also {
        it.erGroupUid = person.personGroupUid
        it.erRoleUid = role.roleUid
        it.erActive = true
        if(it.erUid == 0L) {
            it.erUid = entityRoleDao.insertAsync(it)
        }else {
            entityRoleDao.insertOrReplace(it)
        }
    }
}
