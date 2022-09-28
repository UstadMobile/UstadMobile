
package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.CourseTerminologyDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.util.test.waitUntilAsyncOrTimeout
import com.ustadmobile.core.view.CourseTerminologyEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.TerminologyEntry
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever


/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class CourseTerminologyEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: CourseTerminologyEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoCourseTerminologyDaoSpy: CourseTerminologyDao

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoCourseTerminologyDaoSpy = spy(repo.courseTerminologyDao)
        whenever(repo.courseTerminologyDao).thenReturn(repoCourseTerminologyDaoSpy)
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val presenterArgs = mapOf<String, String>()

        val presenter = CourseTerminologyEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        initialEntity!!.ctTitle = "Professional English"
        whenever(mockView.terminologyTermList).thenReturn(listOf(
            TerminologyEntry("AddStudent",MessageID.standard, "ABC"),
            TerminologyEntry("AddTeacher",MessageID.standard,"XTZ"),
            TerminologyEntry("Student",MessageID.standard,"fsds"),
            TerminologyEntry("Teacher",MessageID.standard,"abc")
        ))
        whenever(mockView.entity).thenReturn(initialEntity)
        presenter.handleClickSave(initialEntity)

        runBlocking {
            db.waitUntil(5000, listOf("CourseTerminology")) {
                db.courseTerminologyDao.findAllCourseTerminologyList().size == 1
            }
        }


        val entitySaved = db.courseTerminologyDao.findAllCourseTerminologyList()[0]
        Assert.assertEquals("Entity was saved to database",  "Professional English",
                entitySaved.ctTitle)
        val label: Map<String,String> = safeParse(di, MapSerializer(String.serializer(), String.serializer()), entitySaved!!.ctTerminology!!)
        Assert.assertEquals("Name was saved and updated",
            "ABC", label["AddStudent"])
    }

    @Test
    fun givenExistingCourseTerminology_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val db: UmAppDatabase by di.activeDbInstance()

        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = CourseTerminology().apply {
            ctTitle = "Standard English"
            ctTerminology = safeStringify(di,
                MapSerializer(String.serializer(), String.serializer()),
                mapOf("AddStudent" to "ABC",
                    "AddTeacher" to "XTZ",
                    "Student" to "fsds",
                    "Teacher" to "abc"))
            ctUid = repo.courseTerminologyDao.insert(this)
        }


        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.ctUid.toString())
        val presenter = CourseTerminologyEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity!!.ctTitle = "Professional English"

        whenever(mockView.entity).thenReturn(initialEntity)
        whenever(mockView.terminologyTermList).thenReturn(listOf(
            TerminologyEntry("AddStudent",MessageID.standard,"Add Student"),
            TerminologyEntry("AddTeacher",MessageID.standard,"XTZ"),
            TerminologyEntry("Student",MessageID.standard,"fsds"),
            TerminologyEntry("Teacher",MessageID.standard,"abc")))

        presenter.handleClickSave(initialEntity)


        runBlocking {
            db.waitUntilAsyncOrTimeout(5000, listOf("CourseTerminology")) {
                db.courseTerminologyDao.findByUidAsync(testEntity.ctUid)?.ctTitle == "Professional English"
            }
        }

        runBlocking {
            val entitySaved = db.courseTerminologyDao.findByUidAsync(testEntity.ctUid)
            val label: Map<String,String> = safeParse(di, MapSerializer(String.serializer(), String.serializer()), entitySaved!!.ctTerminology!!)
            Assert.assertEquals("Name was saved and updated",
                "Add Student", label["AddStudent"])

            Assert.assertEquals("Name was saved and updated",
                "Professional English", entitySaved.ctTitle)
        }

    }


}