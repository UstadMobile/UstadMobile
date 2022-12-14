
package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.CourseGroupSetDao
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.view.CourseGroupSetEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CourseGroupSet
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.mockito.kotlin.*

class CourseGroupSetEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: CourseGroupSetEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoCourseGroupSetDaoSpy: CourseGroupSetDao


    private lateinit var clazz: Clazz

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoCourseGroupSetDaoSpy = spy(repo.courseGroupSetDao)
        whenever(repo.courseGroupSetDao).thenReturn(repoCourseGroupSetDaoSpy)

        //TODO: insert any entities required for all tests
        clazz = Clazz().apply{
            clazzUid = repo.clazzDao.insert(this)
        }
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val presenterArgs = mapOf(
            ARG_CLAZZUID to clazz.clazzUid.toString())

        val presenter = CourseGroupSetEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //TODO: Make some changes (e.g. as the user would do using data binding
        //e.g. initialEntity.someNameField = "Bob"
        initialEntity.cgsName = "Test Group"
        initialEntity.cgsTotalGroups = 2

        presenter.handleClickSave(initialEntity)

        runBlocking {
            db.waitUntil(5000, listOf("CourseGroupSet")) {
                db.courseGroupSetDao.findAllCourseGroupSetForClazzList(clazz.clazzUid).isNotEmpty()
            }
        }

        val entity = db.courseGroupSetDao.findAllCourseGroupSetForClazzList(clazz.clazzUid)[0]
        Assert.assertEquals("Entity was saved to database", "Test Group",
            entity.cgsName)
        Assert.assertEquals("Total groups saved", 2,
                entity.cgsTotalGroups)



    }

    @Test
    fun givenExistingCourseGroupSet_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testEntity = CourseGroupSet().apply {
            cgsName = "Spelling Clazz"
            cgsClazzUid = clazz.clazzUid
            cgsUid = repo.courseGroupSetDao.insert(this)
        }

        val presenterArgs = mapOf(
            ARG_ENTITY_UID to testEntity.cgsUid.toString(),
            ARG_CLAZZUID to testEntity.cgsClazzUid.toString())
        val presenter = CourseGroupSetEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity!!.cgsName = "Test Group"

        presenter.handleClickSave(initialEntity)

        runBlocking {
            db.waitUntil(5000, listOf("CourseGroupSet")) {
                db.courseGroupSetDao.findAllCourseGroupSetForClazzList(clazz.clazzUid).isNotEmpty()
            }
        }

        val entity = db.courseGroupSetDao.findAllCourseGroupSetForClazzList(clazz.clazzUid)[0]
        Assert.assertEquals("Entity was saved to database", "Test Group",
            entity.cgsName)

    }


}
