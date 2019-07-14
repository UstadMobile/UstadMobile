package com.ustadmobile.port.android.view

import android.content.Intent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.platform.app.InstrumentationRegistry
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.generated.MessageIDMap
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewReportEspressoTest {

    @get:Rule
    val mActivityRule = IntentsTestRule(XapiReportOptionsActivity::class.java, false, false)

    private var context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    @Before
    fun setup() {
        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP
        db = UmAppDatabase.getInstance(context)
        repo = db //db!!.getRepository("http://localhost/dummy/", "")
        db.clearAllTables()

        val objectDao = db.xObjectDao
        val entryLangMap = db.xLangMapEntryDao
        val personDao = db.personDao
        val verbDao = db.verbDao
        val locationDao = db.locationDao
        val entryDao = db.contentEntryDao
        val entryJoinDao = db.contentEntryParentChildJoinDao
        val statementDao = db.statementDao

        var firstPerson = Person()
        firstPerson.firstNames = "Hello"
        firstPerson.lastName = "World"
        firstPerson.gender = Person.GENDER_MALE
        firstPerson.personUid = personDao.insert(firstPerson)

        var secondPerson = Person()
        secondPerson.firstNames = "Here"
        secondPerson.lastName = "Now"
        secondPerson.gender = Person.GENDER_MALE
        secondPerson.personUid = personDao.insert(secondPerson)

        var thirdPerson = Person()
        thirdPerson.firstNames = "Lots"
        thirdPerson.lastName = "Maker"
        thirdPerson.gender = Person.GENDER_FEMALE
        thirdPerson.personUid = personDao.insert(thirdPerson)

        var fourthPerson = Person()
        fourthPerson.firstNames = "Never"
        fourthPerson.lastName = "Give"
        fourthPerson.gender = Person.GENDER_OTHER
        fourthPerson.personUid = personDao.insert(fourthPerson)

        var firstVerb = VerbEntity()
        firstVerb.urlId =  "Did"
        firstVerb.verbUid = verbDao.insert(firstVerb)

        var firstVerbLangMap = XLangMapEntry( firstVerb.verbUid , 0, 0, 0, "Did")
        firstVerbLangMap.languageLangMapUid =entryLangMap.insert(firstVerbLangMap)

        var secondVerb = VerbEntity()
        secondVerb.urlId =  "This"
        secondVerb.verbUid = verbDao.insert(secondVerb)

        var secondVerbLangMap = XLangMapEntry(secondVerb.verbUid, 0, 0, 0, "This")
        secondVerbLangMap.languageLangMapUid = entryLangMap.insert(secondVerbLangMap)

        var thirdVerb = VerbEntity()
        thirdVerb.urlId =  "Thing"
        thirdVerb.verbUid = verbDao.insert(thirdVerb)

        var thirdVerbLangMap = XLangMapEntry(thirdVerb.verbUid, 0, 0, 0, "Thing")
        thirdVerbLangMap.languageLangMapUid = entryLangMap.insert(thirdVerbLangMap)


        var newLocation = Location("Test Location", "Test location added from Dummy data", false, 0)
        newLocation.locationUid = locationDao.insert(newLocation)

        val afg = Location("Afghanistan",
                "Afghanistan whole region", true)
        val afgLocationUid = locationDao.insert(afg)
        val centralAfg = Location("Central Afghanistan", "Center region", true, afgLocationUid)
        val centralAfgLocationUid = locationDao.insert(centralAfg)
        val easternAfg = Location("Eastern Afghanistan", "Eastern region", true, afgLocationUid)
        locationDao.insert(Location("Kabul Province",
                "Kabul area", true, centralAfgLocationUid))

        locationDao.insert(easternAfg)
        val northernAfg = Location("Northern Afghanistan", "Northern region", true, afgLocationUid)
        val northernAfgLocationUid = locationDao.insert(northernAfg)
        val westernAfg = Location("Western Afghanistan", "Western region", true, afgLocationUid)
        locationDao.insert(Location("Kunduz Province",
                "Kunduz area", true, northernAfgLocationUid))

        val westernAfgLocationUid = locationDao.insert(westernAfg)
        val southeastAfg = Location("Southeast Afghanistan", "Southeast region", true, afgLocationUid)
        locationDao.insert(Location("Herat Province",
                "Herat area", true, westernAfgLocationUid))
        val southeastAfgLocationUid = locationDao.insert(southeastAfg)
        locationDao.insert(Location("Khost Province",
                "Khost area", true, southeastAfgLocationUid))
        locationDao.insert(Location("Paktika Province",
                "Paktika area", true, southeastAfgLocationUid))

        val southWestAfg = Location("Southwest Afghanistan",
                "Southwest region", true, afgLocationUid)
        locationDao.insert(southWestAfg)

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
        khanclass1.contentEntryUid = entryDao.insert(khanclass1)

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
        firstObject.objectId =  "hello"
        firstObject.objectContentEntryUid = khanclass1.contentEntryUid
                firstObject.xObjectUid = objectDao.insert(firstObject)

        var firstObjectLangMap = XLangMapEntry(0, firstObject.xObjectUid, 0, 0, "Answer")
        firstObjectLangMap.languageLangMapUid = entryLangMap.insert(firstObjectLangMap)

        var secondObject = XObjectEntity()
        secondObject.objectId =  "world"
        secondObject.objectContentEntryUid = khanclass2.contentEntryUid
        secondObject.xObjectUid = objectDao.insert(secondObject)

        var secondObjectLangMap = XLangMapEntry(0, secondObject.xObjectUid, 0, 0, "Me")
        secondObjectLangMap.languageLangMapUid = entryLangMap.insert(secondObjectLangMap)

        var thirdObject = XObjectEntity()
        thirdObject.objectId =  "now"
        thirdObject.objectContentEntryUid = khanclass1.contentEntryUid
        thirdObject.xObjectUid = objectDao.insert(thirdObject)

        var thirdObjectLangMap = XLangMapEntry(0, thirdObject.xObjectUid, 0, 0, "Now")
        thirdObjectLangMap.languageLangMapUid = entryLangMap.insert(thirdObjectLangMap)


        var firstStatement = StatementEntity()
        firstStatement.personUid = firstPerson.personUid
        firstStatement.resultDuration = 1000
        firstStatement.resultScoreScaled = 50
        firstStatement.verbUid = firstVerb.verbUid
        firstStatement.xObjectUid = firstObject.xObjectUid
        firstStatement.timestamp = DateTime(2019, 6, 11).unixMillisLong
        firstStatement.statementUid = statementDao.insert(firstStatement)


        var secondStaement = StatementEntity()
        secondStaement.personUid = firstPerson.personUid
        secondStaement.resultDuration = 500
        secondStaement.resultScoreScaled = 100
        secondStaement.verbUid = secondVerb.verbUid
        secondStaement.xObjectUid = firstObject.xObjectUid
        secondStaement.timestamp = DateTime(2019, 5, 1).unixMillisLong
        secondStaement.statementUid = statementDao.insert(secondStaement)


        var thirdStatement = StatementEntity()
        thirdStatement.personUid = secondPerson.personUid
        thirdStatement.resultDuration = 1000
        thirdStatement.resultScoreScaled = 50
        thirdStatement.verbUid = firstVerb.verbUid
        thirdStatement.xObjectUid = secondObject.xObjectUid
        thirdStatement.timestamp = DateTime(2019, 4, 10).unixMillisLong
        thirdStatement.statementUid = statementDao.insert(thirdStatement)

        var fourthStatement = StatementEntity()
        fourthStatement.personUid = thirdPerson.personUid
        fourthStatement.resultDuration = 800
        fourthStatement.resultScoreScaled = 20
        fourthStatement.verbUid = firstVerb.verbUid
        fourthStatement.xObjectUid = secondObject.xObjectUid
        fourthStatement.timestamp = DateTime(2019, 6, 30).unixMillisLong
        fourthStatement.statementUid = statementDao.insert(fourthStatement)


        var fifthStatement = StatementEntity()
        fifthStatement.personUid = fourthPerson.personUid
        fifthStatement.resultDuration = 100
        fifthStatement.resultScoreScaled = 85
        fifthStatement.verbUid = thirdVerb.verbUid
        fifthStatement.xObjectUid = firstObject.xObjectUid
        fifthStatement.timestamp = DateTime(2019, 7, 10).unixMillisLong
        fifthStatement.statementUid = statementDao.insert(fifthStatement)


        var sixthStatement = StatementEntity()
        sixthStatement.personUid = thirdPerson.personUid
        sixthStatement.resultDuration = 700
        sixthStatement.resultScoreScaled = 25
        sixthStatement.verbUid = firstVerb.verbUid
        sixthStatement.xObjectUid = secondObject.xObjectUid
        sixthStatement.timestamp = DateTime(2019, 5, 25).unixMillisLong
        sixthStatement.statementUid = statementDao.insert(sixthStatement)


        var seventhStatement = StatementEntity()
        seventhStatement.personUid = secondPerson.personUid
        seventhStatement.resultDuration = 2000
        seventhStatement.resultScoreScaled = 5
        seventhStatement.verbUid = firstVerb.verbUid
        seventhStatement.xObjectUid = firstObject.xObjectUid
        seventhStatement.timestamp = DateTime(2019, 6, 11).unixMillisLong
        seventhStatement.statementUid = statementDao.insert(seventhStatement)


        val intent = Intent()
        mActivityRule.launchActivity(intent)
    }

    @Test
    fun test(){



    }


}