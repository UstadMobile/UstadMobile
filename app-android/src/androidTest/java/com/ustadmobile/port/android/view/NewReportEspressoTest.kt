package com.ustadmobile.port.android.view

import android.content.Intent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.platform.app.InstrumentationRegistry
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.VerbEntity
import com.ustadmobile.lib.db.entities.XLangMapEntry
import com.ustadmobile.lib.db.entities.XObjectEntity
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


        val intent = Intent()
        mActivityRule.launchActivity(intent)
    }

    @Test
    fun test(){

    }


}