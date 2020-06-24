package com.ustadmobile.util.test.ext

import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.Companion.REL_TYPE_TRANSLATED_VERSION
import com.ustadmobile.lib.util.getSystemTimeInMillis

data class TestClazzAndMembers(val clazz: Clazz, val teacherList: List<ClazzMember>, val studentList: List<ClazzMember>)

private fun Person.asClazzMember(clazzUid: Long, clazzMemberRole: Int, joinTime: Long): ClazzMember {
    return ClazzMember(clazzUid, this.personUid, clazzMemberRole).apply {
        clazzMemberDateJoined = joinTime
    }
}

suspend fun UmAppDatabase.insertTestClazzAndMembers(numClazzStudents: Int, numClazzTeachers: Int = 1,
                                                    clazzJoinTime: Long = (getSystemTimeInMillis() -  (86400* 1000)),
    studentNamer: (Int) -> Pair<String, String> = {"Test" to "Student $it"},
    teacherNamer: (Int) -> Pair<String, String> = {"Test" to "Teacher $it"}): TestClazzAndMembers {
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
        numEntries: Int, parentEntryUid: Long, nonLeafIndexes: MutableList<Int> = mutableListOf()): List<ContentEntry> {
    return (1 .. numEntries).map {
        val entry = ContentEntry().apply {
            leaf = !(nonLeafIndexes.isNotEmpty() && nonLeafIndexes.indexOf(it - 1) != -1)
            title = "Dummy ${if(leaf) " entry" else "folder"} title $it"
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


suspend fun UmAppDatabase.insertTestStatements() {
    val objectDao = this.xObjectDao
    val entryLangMap = this.xLangMapEntryDao
    val personDao = this.personDao
    val verbDao = this.verbDao
    val entryDao = this.contentEntryDao
    val entryJoinDao = this.contentEntryParentChildJoinDao
    val statementDao = this.statementDao

    var firstPerson = Person()
    firstPerson.firstNames = "Hello"
    firstPerson.lastName = "World"
    firstPerson.gender = Person.GENDER_MALE
    firstPerson.personUid = 100
    personDao.insert(firstPerson)

    var secondPerson = Person()
    secondPerson.firstNames = "Here"
    secondPerson.lastName = "Now"
    secondPerson.gender = Person.GENDER_MALE
    secondPerson.personUid = 101
    personDao.insert(secondPerson)

    var thirdPerson = Person()
    thirdPerson.firstNames = "Lots"
    thirdPerson.lastName = "Maker"
    thirdPerson.gender = Person.GENDER_FEMALE
    thirdPerson.personUid = personDao.insert(thirdPerson)

    var fourthPerson = Person()
    fourthPerson.firstNames = "Never"
    fourthPerson.lastName = "Give"
    fourthPerson.gender = Person.GENDER_FEMALE
    fourthPerson.personUid = personDao.insert(fourthPerson)

    var firstVerb = VerbEntity()
    firstVerb.urlId = "Did"
    firstVerb.verbUid = 200
    verbDao.insert(firstVerb)

    var firstVerbLangMap = XLangMapEntry(firstVerb.verbUid, 0, 0, 0, "Attempted question 3 from Entry 1")
    firstVerbLangMap.languageLangMapUid = entryLangMap.insert(firstVerbLangMap)

    var secondVerb = VerbEntity()
    secondVerb.urlId = "This"
    secondVerb.verbUid = 201
    verbDao.insert(secondVerb)

    var secondVerbLangMap = XLangMapEntry(secondVerb.verbUid, 0, 0, 0, "Attempted question 1 from Entry 1")
    secondVerbLangMap.languageLangMapUid = entryLangMap.insert(secondVerbLangMap)

    var thirdVerb = VerbEntity()
    thirdVerb.urlId = "Thing"
    thirdVerb.verbUid = 202
    verbDao.insert(thirdVerb)

    var thirdVerbLangMap = XLangMapEntry(thirdVerb.verbUid, 0, 0, 0, "Attempted question 5 from Entry 3")
    thirdVerbLangMap.languageLangMapUid = entryLangMap.insert(thirdVerbLangMap)

    var firstEntry = ContentEntry()
    firstEntry.title = "Ustad Mobile"
    firstEntry.contentEntryUid = entryDao.insert(firstEntry)

    var secondEntry = ContentEntry()
    secondEntry.title = "Khan Academy"
    secondEntry.contentEntryUid = entryDao.insert(secondEntry)

    var firstsecondJoin = ContentEntryParentChildJoin()
    firstsecondJoin.cepcjParentContentEntryUid = firstEntry.contentEntryUid
    firstsecondJoin.cepcjChildContentEntryUid = secondEntry.contentEntryUid
    firstsecondJoin.cepcjUid = entryJoinDao.insert(firstsecondJoin)

    var phetEntry = ContentEntry()
    phetEntry.title = "PHET"
    phetEntry.contentEntryUid = entryDao.insert(phetEntry)

    var phetJoin = ContentEntryParentChildJoin()
    phetJoin.cepcjParentContentEntryUid = firstEntry.contentEntryUid
    phetJoin.cepcjChildContentEntryUid = phetEntry.contentEntryUid
    phetJoin.cepcjUid = entryJoinDao.insert(phetJoin)

    var edraakEntry = ContentEntry()
    edraakEntry.title = "EDRAAK"
    edraakEntry.contentEntryUid = entryDao.insert(edraakEntry)

    var edraakJoin = ContentEntryParentChildJoin()
    edraakJoin.cepcjParentContentEntryUid = firstEntry.contentEntryUid
    edraakJoin.cepcjChildContentEntryUid = edraakEntry.contentEntryUid
    edraakJoin.cepcjUid = entryJoinDao.insert(edraakJoin)


    var khanclass1 = ContentEntry()
    khanclass1.title = "Class 1"
    khanclass1.contentEntryUid = 400
    entryDao.insert(khanclass1)

    var khanclassJoin = ContentEntryParentChildJoin()
    khanclassJoin.cepcjParentContentEntryUid = secondEntry.contentEntryUid
    khanclassJoin.cepcjChildContentEntryUid = khanclass1.contentEntryUid
    khanclassJoin.cepcjUid = entryJoinDao.insert(khanclassJoin)

    var khanclass2 = ContentEntry()
    khanclass2.title = "Class 2"
    khanclass2.contentEntryUid = entryDao.insert(khanclass2)

    var khanclass2Join = ContentEntryParentChildJoin()
    khanclass2Join.cepcjParentContentEntryUid = secondEntry.contentEntryUid
    khanclass2Join.cepcjChildContentEntryUid = khanclass2.contentEntryUid
    khanclass2Join.cepcjUid = entryJoinDao.insert(khanclass2Join)

    var firstObject = XObjectEntity()
    firstObject.objectId = "hello"
    firstObject.objectContentEntryUid = khanclass1.contentEntryUid
    firstObject.xObjectUid = 300
    objectDao.insert(firstObject)

    var firstObjectLangMap = XLangMapEntry(0, firstObject.xObjectUid, 0, 0, "Answer")
    firstObjectLangMap.languageLangMapUid = entryLangMap.insert(firstObjectLangMap)

    var secondObject = XObjectEntity()
    secondObject.objectId = "world"
    secondObject.objectContentEntryUid = khanclass2.contentEntryUid
    secondObject.xObjectUid = 301
    objectDao.insert(secondObject)

    var secondObjectLangMap = XLangMapEntry(0, secondObject.xObjectUid, 0, 0, "Me")
    secondObjectLangMap.languageLangMapUid = entryLangMap.insert(secondObjectLangMap)

    var thirdObject = XObjectEntity()
    thirdObject.objectId = "now"
    thirdObject.objectContentEntryUid = khanclass1.contentEntryUid
    thirdObject.xObjectUid = 302
    objectDao.insert(thirdObject)

    var thirdObjectLangMap = XLangMapEntry(0, thirdObject.xObjectUid, 0, 0, "Now")
    thirdObjectLangMap.languageLangMapUid = entryLangMap.insert(thirdObjectLangMap)


    var firstStatement = StatementEntity()
    firstStatement.statementPersonUid = firstPerson.personUid
    firstStatement.resultDuration = 2400000
    firstStatement.resultScoreScaled = 50
    firstStatement.statementVerbUid = firstVerb.verbUid
    firstStatement.xObjectUid = firstObject.xObjectUid
    firstStatement.resultSuccess = StatementEntity.RESULT_FAILURE
    firstStatement.timestamp = DateTime(2019, 6, 11).unixMillisLong
    firstStatement.statementUid = statementDao.insert(firstStatement)


    var secondStaement = StatementEntity()
    secondStaement.statementPersonUid = firstPerson.personUid
    secondStaement.resultDuration = 7200000
    secondStaement.resultScoreScaled = 100
    secondStaement.statementVerbUid = secondVerb.verbUid
    secondStaement.xObjectUid = firstObject.xObjectUid
    secondStaement.resultSuccess = StatementEntity.RESULT_FAILURE
    secondStaement.timestamp = DateTime(2019, 5, 1).unixMillisLong
    secondStaement.statementUid = statementDao.insert(secondStaement)


    var thirdStatement = StatementEntity()
    thirdStatement.statementPersonUid = secondPerson.personUid
    thirdStatement.resultDuration = 600000
    thirdStatement.resultScoreScaled = 50
    thirdStatement.statementVerbUid = firstVerb.verbUid
    thirdStatement.xObjectUid = secondObject.xObjectUid
    thirdStatement.resultSuccess = StatementEntity.RESULT_FAILURE
    thirdStatement.timestamp = DateTime(2019, 4, 10).unixMillisLong
    thirdStatement.statementUid = statementDao.insert(thirdStatement)

    var fourthStatement = StatementEntity()
    fourthStatement.statementPersonUid = thirdPerson.personUid
    fourthStatement.resultDuration = 120000
    fourthStatement.resultScoreScaled = 20
    fourthStatement.statementVerbUid = firstVerb.verbUid
    fourthStatement.xObjectUid = secondObject.xObjectUid
    fourthStatement.resultSuccess = StatementEntity.RESULT_FAILURE
    fourthStatement.timestamp = DateTime(2019, 6, 30).unixMillisLong
    fourthStatement.statementUid = statementDao.insert(fourthStatement)


    var fifthStatement = StatementEntity()
    fifthStatement.statementPersonUid = fourthPerson.personUid
    fifthStatement.resultDuration = 100000
    fifthStatement.resultScoreScaled = 85
    fifthStatement.statementVerbUid = thirdVerb.verbUid
    fifthStatement.xObjectUid = firstObject.xObjectUid
    fifthStatement.resultSuccess = StatementEntity.RESULT_FAILURE
    fifthStatement.timestamp = DateTime(2019, 7, 10).unixMillisLong
    fifthStatement.statementUid = statementDao.insert(fifthStatement)


    var sixthStatement = StatementEntity()
    sixthStatement.statementPersonUid = thirdPerson.personUid
    sixthStatement.resultDuration = 60000
    sixthStatement.resultScoreScaled = 25
    sixthStatement.statementVerbUid = firstVerb.verbUid
    sixthStatement.resultSuccess = StatementEntity.RESULT_FAILURE
    sixthStatement.xObjectUid = secondObject.xObjectUid
    sixthStatement.timestamp = DateTime(2019, 5, 25).unixMillisLong
    sixthStatement.statementUid = statementDao.insert(sixthStatement)


    var seventhStatement = StatementEntity()
    seventhStatement.statementPersonUid = secondPerson.personUid
    seventhStatement.resultDuration = 30000
    seventhStatement.resultScoreScaled = 5
    seventhStatement.statementVerbUid = firstVerb.verbUid
    seventhStatement.xObjectUid = firstObject.xObjectUid
    seventhStatement.resultSuccess = StatementEntity.RESULT_FAILURE
    seventhStatement.timestamp = DateTime(2019, 6, 11).unixMillisLong
    seventhStatement.statementUid = statementDao.insert(seventhStatement)

    var i = 0
    while (i < 100) {
        var statement = StatementEntity()
        statement.statementPersonUid = secondPerson.personUid
        statement.resultDuration = 30000
        statement.resultScoreScaled = 5
        statement.statementVerbUid = firstVerb.verbUid
        statement.xObjectUid = firstObject.xObjectUid
        statement.resultSuccess = StatementEntity.RESULT_SUCCESS
        statement.timestamp = DateTime(2019, 6, 11).unixMillisLong
        statement.statementUid = statementDao.insert(statement)
        i++
    }
}
