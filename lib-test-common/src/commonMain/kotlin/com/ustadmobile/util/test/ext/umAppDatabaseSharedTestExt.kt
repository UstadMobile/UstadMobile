package com.ustadmobile.util.test.ext

import com.ustadmobile.core.db.UmAppDatabase


fun UmAppDatabase.insertTestStatementsForReports() {
/*
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
        verbUid = VerbEntity.VERB_COMPLETED_UID

    }

    val firstVerbLangMap = XLangMapEntry(completedVerb.verbUid, 0, 0, 0, "completed Entry 1")
    firstVerbLangMap.languageLangMapUid = xLangMapEntryDao.insert(firstVerbLangMap)

    val passedVerb = VerbEntity().apply {
        verbUid = VerbEntity.VERB_PASSED_UID
    }

    val secondVerbLangMap = XLangMapEntry(passedVerb.verbUid, 0, 0, 0, "Passed Entry 1")
    secondVerbLangMap.languageLangMapUid = xLangMapEntryDao.insert(secondVerbLangMap)

    val failedVerb = VerbEntity().apply {

        verbUid = VerbEntity.VERB_FAILED_UID

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

    var firstObject = ActivityEntity()
    firstObject.objectId = "hello"
    
    firstObject.activityUid =  activityEntityDao.insert(firstObject)


    var firstObjectLangMap = XLangMapEntry(0, firstObject.activityUid, 0, 0, khanclass1.title!!)
    firstObjectLangMap.languageLangMapUid = xLangMapEntryDao.insert(firstObjectLangMap)

    var secondObject = ActivityEntity()
    secondObject.objectId = "world"
    secondObject.activityUid =  activityEntityDao.insert(secondObject)


    var secondObjectLangMap = XLangMapEntry(0, secondObject.activityUid, 0, 0, khanclass2.title!!)
    secondObjectLangMap.languageLangMapUid = xLangMapEntryDao.insert(secondObjectLangMap)

    var thirdObject = ActivityEntity()
    thirdObject.objectId = "now"
    thirdObject.activityUid =  activityEntityDao.insert(thirdObject)


    var thirdObjectLangMap = XLangMapEntry(0, thirdObject.activityUid, 0, 0, khanclass1.title!!)
    thirdObjectLangMap.languageLangMapUid = xLangMapEntryDao.insert(thirdObjectLangMap)
    
    StatementEntity().apply {
        statementActorPersonUid = firstPerson.personUid
        resultDuration = 2400000
        resultCompletion = true
        resultScoreScaled = 50f
        
        statementVerbUid = completedVerb.verbUid
        xObjectUid = firstObject.activityUid
        statementContentEntryUid = khanclass1.contentEntryUid
        timestamp = DateTime(2019, 6, 11).unixMillisLong
        contentEntryRoot = true
    }
  
    StatementEntity().apply {
        statementActorPersonUid = firstPerson.personUid
        resultDuration = 7200000
        resultScoreScaled = 100f
        resultCompletion = true
        
        statementVerbUid = passedVerb.verbUid
        xObjectUid = firstObject.activityUid
        statementContentEntryUid = khanclass1.contentEntryUid

        timestamp = DateTime(2019, 5, 1).unixMillisLong
        contentEntryRoot = false
    }

    val commonSessionForSecondPerson =randomUuid().toString()

    StatementEntity().apply {
        statementActorPersonUid = secondPerson.personUid
        resultDuration = 600000
        resultScoreScaled = 50f
        resultCompletion = true
        statementVerbUid = completedVerb.verbUid
        xObjectUid = secondObject.activityUid
        statementContentEntryUid = khanclass2.contentEntryUid
        timestamp = DateTime(2019, 4, 10).unixMillisLong
        contentEntryRoot = true
    }
    

    val commonSession =randomUuid().toString()
    StatementEntity().apply {
        statementActorPersonUid = thirdPerson.personUid
        resultDuration = 120000
        resultScoreScaled = 20f
        resultCompletion = true
        statementVerbUid = completedVerb.verbUid
        xObjectUid = secondObject.activityUid
        statementContentEntryUid = khanclass2.contentEntryUid
        timestamp = DateTime(2019, 6, 30).unixMillisLong
        contentEntryRoot = true
    }
    


    StatementEntity().apply {
        statementActorPersonUid = fourthPerson.personUid
        resultDuration = 100000
        resultScoreScaled = 85f
        resultCompletion = true
        
        statementVerbUid = failedVerb.verbUid
        xObjectUid = firstObject.activityUid
        statementContentEntryUid = khanclass1.contentEntryUid
        timestamp = DateTime(2019, 7, 10).unixMillisLong
        contentEntryRoot = true
    }
  


    StatementEntity().apply {
        statementActorPersonUid = thirdPerson.personUid
        resultDuration = 60000
        resultScoreScaled = 25f
        resultCompletion = true
        statementVerbUid = completedVerb.verbUid
        xObjectUid = secondObject.activityUid
        statementContentEntryUid = khanclass2.contentEntryUid
        timestamp = DateTime(2019, 5, 25).unixMillisLong
        contentEntryRoot = true
    }


    StatementEntity().apply {
        statementActorPersonUid = secondPerson.personUid
        resultDuration = 30000
        resultScoreScaled = 5f
        resultCompletion = true
        statementVerbUid = completedVerb.verbUid
        xObjectUid = firstObject.activityUid
        statementContentEntryUid = khanclass1.contentEntryUid
        timestamp = DateTime(2019, 6, 11).unixMillisLong
        contentEntryRoot = true

    }

    for(i in 0..10){
        StatementEntity().apply {
            statementActorPersonUid = secondPerson.personUid
            resultDuration = 30000
            resultScoreScaled = 5f
            resultCompletion = false
            
            statementVerbUid = completedVerb.verbUid
            xObjectUid = thirdObject.activityUid
            statementContentEntryUid = khanclass1.contentEntryUid
            timestamp = DateTime(2019, 6, 11).unixMillisLong
        }
    }*/
}

