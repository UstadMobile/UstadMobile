package com.ustadmobile.port.android.view

import android.content.Intent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.platform.app.InstrumentationRegistry
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

        var firstPerson = Person()
        firstPerson.firstNames = "Hello"
        firstPerson.lastName = "World"
        personDao.insert(firstPerson)

        var secondPerson = Person()
        secondPerson.firstNames = "Here"
        secondPerson.lastName = "Now"
        personDao.insert(secondPerson)

        var thirdPerson = Person()
        thirdPerson.firstNames = "Lots"
        thirdPerson.lastName = "Maker"
        personDao.insert(thirdPerson)

        var fourthPerson = Person()
        fourthPerson.firstNames = "Never"
        fourthPerson.lastName = "Give"
        personDao.insert(fourthPerson)

        var firstVerb = VerbEntity()
        firstVerb.urlId =  "Did"
        firstVerb.verbUid = verbDao.insert(firstVerb)

        var firstVerbLangMap = XLangMapEntry( firstVerb.verbUid , 0, 0, 0, "Did")
        entryLangMap.insert(firstVerbLangMap)

        var secondVerb = VerbEntity()
        secondVerb.urlId =  "This"
        secondVerb.verbUid = verbDao.insert(secondVerb)

        var secondVerbLangMap = XLangMapEntry(secondVerb.verbUid, 0, 0, 0, "This")
        entryLangMap.insert(secondVerbLangMap)

        var thirdVerb = VerbEntity()
        thirdVerb.urlId =  "Thing"
        thirdVerb.verbUid = verbDao.insert(thirdVerb)

        var thirdVerbLangMap = XLangMapEntry(thirdVerb.verbUid, 0, 0, 0, "Thing")
        entryLangMap.insert(thirdVerbLangMap)

        var firstObject = XObjectEntity()
        firstObject.objectId =  "hello"
        firstObject.xObjectUid = objectDao.insert(firstObject)

        var firstObjectLangMap = XLangMapEntry(0, firstObject.xObjectUid, 0, 0, "Answer")
        entryLangMap.insert(firstObjectLangMap)

        var secondObject = XObjectEntity()
        secondObject.objectId =  "world"
        secondObject.xObjectUid = objectDao.insert(secondObject)

        var secondObjectLangMap = XLangMapEntry(0, secondObject.xObjectUid, 0, 0, "Me")
        entryLangMap.insert(secondObjectLangMap)

        var thirdObject = XObjectEntity()
        thirdObject.objectId =  "now"
        thirdObject.xObjectUid = objectDao.insert(thirdObject)

        var thirdObjectLangMap = XLangMapEntry(0, thirdObject.xObjectUid, 0, 0, "Now")
        entryLangMap.insert(thirdObjectLangMap)

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

        val intent = Intent()
        mActivityRule.launchActivity(intent)
    }

    @Test
    fun test(){

    }


}