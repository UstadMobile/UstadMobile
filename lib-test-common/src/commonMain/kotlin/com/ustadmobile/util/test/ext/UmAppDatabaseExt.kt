package com.ustadmobile.util.test.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.Companion.REL_TYPE_TRANSLATED_VERSION
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlin.random.Random

data class TestClazzAndMembers (val clazz: Clazz, val teacherList: List<ClazzMember>, val studentList: List<ClazzMember>)
data class TestClazzWork(val clazzAndMembers: TestClazzAndMembers, val clazzWork: ClazzWork,
            val quizQuestionsAndOptions: TestClazzWorkWithQuestionAndOptionsAndResponse? = null)
data class TestClazzWorkWithQuestionAndOptionsAndResponse(val clazzWork: ClazzWork,
      val questionsAndOptions: List<ClazzWorkQuestionAndOptions>, val responses: List<ClazzWorkQuestionResponse?>)
data class TestContentAndJoin(val contentList : List<ContentEntry> ,
                               val joinList: List<ClazzWorkContentJoin>)

private fun Person.asClazzMember(clazzUid: Long, clazzMemberRole: Int, joinTime: Long): ClazzMember {
    return ClazzMember(clazzUid, this.personUid, clazzMemberRole).apply {
        clazzMemberDateJoined = joinTime
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
            commentsPersonUid = student.clazzMemberPersonUid
            commentsInActive = false
            commentsDateTimeAdded = dateNow
            commentsUid = commentsDao.insertAsync(this)

        }
    }

}

suspend fun UmAppDatabase.insertQuizQuestionsAndOptions(
        clazzWork: ClazzWork, responded: Boolean = false, clazzMemberUid: Long = 0, personUid: Long = 0,
        quizQuestionType: Int, quizQuestionTypeMixed: Boolean = false): TestClazzWorkWithQuestionAndOptionsAndResponse {

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
                        }
                    }
        }


        if (responded && clazzMemberUid != 0L && personUid != 0L ) {
            //Create question response
            for ((index, question) in clazzWorkQuestionsAndOptions.withIndex()) {
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
                            clazzWorkQuestionResponseClazzMemberUid = clazzMemberUid
                            clazzWorkQuestionResponseInactive = false
                            //TODO: Dates
                            clazzWorkQuestionResponseDateResponded = 0

                            clazzWorkQuestionResponseUid = clazzWorkQuestionResponseDao.insertAsync(this)
                        }
                responses.add(response)
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

suspend fun UmAppDatabase.createTestContentEntries(num: Int): List<ContentEntry>{

    return (1 .. num).map {
        ContentEntry().apply {
            title = "Content  $it"
            description = "Content description $it"
            entryId = "42$it"
            author = "Mr.Tester McTestface"
            publik = true
            publisher = "TestCorp"
            leaf = true
            contentEntryUid = contentEntryDao.insertAsync(this)
        }
    }
}

suspend fun UmAppDatabase.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
        clazzWork: ClazzWork, responded : Boolean = false, submissionType: Int = -1 ,
        quizQuestionTypeMixed: Boolean = false, quizQuestionType: Int = 0,
        submitted: Boolean = false, isStudentToClazz : Boolean = false
    ):TestClazzWork {
    val clazzAndMembers = insertTestClazzAndMembers(5, 2)
    clazzWork.apply{
        clazzWorkTitle = "Clazz Work A"
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
    }

    //Getting member
    val clazzMember: ClazzMember

    val studentClazzMember = clazzAndMembers.studentList.get(0)

    if(isStudentToClazz){
        clazzMember = clazzAndMembers.studentList.get(0)
    }else{
        clazzMember = clazzAndMembers.teacherList.get(0)
    }

    var quizQuestionsAndOptions: TestClazzWorkWithQuestionAndOptionsAndResponse? = null
    if(clazzWork.clazzWorkSubmissionType == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ) {
        quizQuestionsAndOptions = insertQuizQuestionsAndOptions(clazzWork, responded, studentClazzMember.clazzMemberUid,
            studentClazzMember.clazzMemberPersonUid, quizQuestionType, quizQuestionTypeMixed)
    }

    //Create Submission
    if(submitted){
        ClazzWorkSubmission().apply {
            clazzWorkSubmissionClazzWorkUid = clazzWork.clazzWorkUid
            clazzWorkSubmissionClazzMemberUid = clazzMember.clazzMemberUid
            clazzWorkSubmissionPersonUid = clazzMember.clazzMemberPersonUid
            clazzWorkSubmissionInactive = false
            //TODO: dates
            clazzWorkSubmissionDateTimeStarted = 0
            clazzWorkSubmissionDateTimeUpdated = 0
            clazzWorkSubmissionDateTimeFinished = 0
            if(submissionType == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT){
                clazzWorkSubmissionText = "This is the test submission"
            }
            clazzWorkSubmissionScore = 89
            clazzWorkSubmissionUid = clazzWorkSubmissionDao.insertAsync(this)
        }
    }


    return TestClazzWork(clazzAndMembers, clazzWork, quizQuestionsAndOptions)
}

suspend fun UmAppDatabase.insertTestClazzAndMembers(numClazzStudents: Int, numClazzTeachers: Int = 1,
    studentNamer: (Int) -> Pair<String, String> = {"Test" to "Student $it"},
    teacherNamer: (Int) -> Pair<String, String> = {"Test" to "Teacher $it"}): TestClazzAndMembers {
    val mockClazz = Clazz("Test Clazz").apply {
        clazzTimeZone = "Asia/Dubai"
        clazzUid = clazzDao.insertAsync(this)
    }

    val testStudents = (1 .. numClazzStudents).map {
        val (firstName, lastName) = studentNamer(it)
        Person("studentuser$it", firstName, lastName).apply {
            personUid = personDao.insertAsync(this)
        }
    }

    val testTeachers = (1 .. numClazzTeachers).map {
        val (firstName, lastName) = teacherNamer(it)
        Person("studentuser$it", firstName, lastName).apply {
            personUid = personDao.insertAsync(this)
        }
    }

    val clazzJoinTime = getSystemTimeInMillis() - 1000

    val testStudentClazzMembers = testStudents.map {
        it.asClazzMember(mockClazz.clazzUid, ClazzMember.ROLE_STUDENT, clazzJoinTime).apply {
            clazzMemberUid = clazzMemberDao.insertAsync(this)
        }
    }

    val testTeacherClazzMembers = testTeachers.map {
        it.asClazzMember(mockClazz.clazzUid, ClazzMember.ROLE_TEACHER, clazzJoinTime).apply {
            clazzMemberUid = clazzMemberDao.insertAsync(this)
        }
    }

    return TestClazzAndMembers(mockClazz, testTeacherClazzMembers, testStudentClazzMembers)
}

suspend fun UmAppDatabase.insertClazzLogs(clazzUid: Long, numLogs: Int, logMaker: (Int) -> ClazzLog): List<ClazzLog> {
    return (0 until numLogs).map {index ->
        logMaker(index).apply {
            clazzLogClazzUid = clazzUid
            clazzLogUid = clazzLogDao.insertAsync(this)
        }
    }
}

suspend fun UmAppDatabase.insertContentEntryWithTranslations(numTranslations: Int,entryUid: Long): ContentEntry{
    val entry = ContentEntry().apply {
        title = "Dummy Content Entry"
        leaf = true
        description = "Dummy Entry description"
        contentEntryUid = entryUid
        contentEntryDao.insertAsync(this)
    }

     (1 .. numTranslations).map {
        val entryOfLanguage = ContentEntry().apply {
            title = "Language $it Content Entry"
            leaf = true
            description = "Dummy Entry description language $it"
            contentEntryUid = contentEntryDao.insertAsync(this)
        }
        val language = Language().apply {
            name = "Language $it"
            iso_639_2_standard = "${if(it >= 10) it else "0$it"}"
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
        numEntries: Int, parentEntryUid: Long, isLeaf: Boolean = true): List<ContentEntry> {
    return (1 .. numEntries).map {
        val entry = ContentEntry().apply {
            title = "Dummy title $it"
            leaf = isLeaf
            description = "Dummy description $it"
            contentEntryUid = contentEntryDao.insertAsync(this)
        }
        ContentEntryParentChildJoin().apply {
            cepcjChildContentEntryUid = entry.contentEntryUid
            cepcjParentContentEntryUid = parentEntryUid
            cepcjUid = contentEntryParentChildJoinDao.insertAsync(this)
        }

        Container().apply {
            fileSize = 10000
            cntLastModified = getSystemTimeInMillis()
            containerContentEntryUid = entry.contentEntryUid
            containerUid = containerDao.insertAsync(this)

        }
        entry
    }
}