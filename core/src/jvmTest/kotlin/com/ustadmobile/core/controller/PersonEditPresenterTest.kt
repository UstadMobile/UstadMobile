package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.ext.getAttachmentUri
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.TYPE_FIELD
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicReference

class PersonEditPresenterTest {

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var dbPersonPictureDao: PersonPictureDao

    private lateinit var repoPersonPictureDao: PersonPictureDao

    private val context = Any()

    private lateinit var systemImpl: UstadMobileSystemImpl

    private lateinit var personDetailPresenterFieldFirstNames: PersonDetailPresenterField

    private lateinit var personDetailPresenterFieldCustomId: PersonDetailPresenterField

    private lateinit var personDetailPresenterFieldCustomDropdown: PersonDetailPresenterField

    private lateinit var personDetailPresenterFieldPicture: PersonDetailPresenterField

    private lateinit var customIdField: CustomField

    private lateinit var customDropDownField: CustomField

    private lateinit var customDropDownFieldOptions: List<CustomFieldValueOption>

    private lateinit var mockView: PersonEditView

    private val tmpFilesToDelete = mutableListOf<File>()

    @Before
    fun setup() {
        checkJndiSetup()
        val realDb = UmAppDatabase.getInstance(context)

        val tmpAttachmentDir = Files.createTempDirectory("personeditpresentertest").toFile()
        val realRepo = realDb.asRepository<UmAppDatabase>(context,
                "http://localhost/dummy", "", defaultHttpClient(),
                tmpAttachmentDir.absolutePath)
        tmpFilesToDelete += tmpAttachmentDir

        dbPersonPictureDao = spy(realDb.personPictureDao)
        repoPersonPictureDao = spy(realRepo.personPictureDao)

        db = spy(realDb) {
            on { personPictureDao }.thenReturn(dbPersonPictureDao)
        }

        repo = spy(realRepo) {
            on { personPictureDao }.thenReturn(repoPersonPictureDao)
        }

        db.clearAllTables()

        personDetailPresenterFieldFirstNames = PersonDetailPresenterField(
                fieldUid = PersonDetailPresenterField.PERSON_FIELD_UID_FIRST_NAMES.toLong(),
                fieldType = TYPE_FIELD, fieldIndex = 0)
        personDetailPresenterFieldCustomId = PersonDetailPresenterField(
                fieldUid = 1042L, fieldType = TYPE_FIELD, fieldIndex = 1)
        personDetailPresenterFieldCustomDropdown = PersonDetailPresenterField(
                fieldUid = 1043L, fieldType = TYPE_FIELD, fieldIndex = 2)
        personDetailPresenterFieldPicture = PersonDetailPresenterField(
                fieldUid = PersonDetailPresenterField.PERSON_FIELD_UID_PICTURE.toLong(),
                fieldType = TYPE_FIELD, fieldIndex = 3)


        customIdField = CustomField(1042L, customFieldName = "Our Custom ID")
        customDropDownField = CustomField(1043L, customFieldName = "Custom Dropdown")
        db.personDetailPresenterFieldDao.insertList(listOf(personDetailPresenterFieldFirstNames,
                personDetailPresenterFieldCustomId, personDetailPresenterFieldCustomDropdown,
                personDetailPresenterFieldPicture))
        db.customFieldDao.insertList(listOf(customIdField, customDropDownField))
        db.customFieldValueOptionDao.insertList(
                listOf(CustomFieldValueOption().apply {
                    customFieldValueOptionFieldUid = 1043L
                    customFieldValueOptionName = "Option 1"
                },
                CustomFieldValueOption().apply {
                    customFieldValueOptionFieldUid = 1043L
                    customFieldValueOptionName = "Option 2"
                }
                ))

        systemImpl = UstadMobileSystemImpl.instance
        mockView = mock { }
    }

    @After
    fun tearDown() {
        tmpFilesToDelete.forEach { it.deleteRecursively() }
        tmpFilesToDelete.clear()
    }

    fun makeTempPic(picNum: Int = 0): String {
        val picTmpFile = File.createTempFile("tmp", "img")
        tmpFilesToDelete += picTmpFile
        extractTestResourceToFile("/com/ustadmobile/core/controller/cat-pic$picNum.jpg", picTmpFile)
        return picTmpFile.absolutePath
    }

    fun createPresenterSetValsAndSave(args: Map<String, String>, block: (DoorMutableLiveData<List<PresenterFieldRow>>) -> Unit): Person {
        val mockLifecycle = mock<DoorLifecycleOwner> {}
        val presenter = PersonEditPresenter(context, args, mockView, mockLifecycle,
                systemImpl, db, repo, UmAccountManager.activeAccountLiveData)
        presenter.onCreate(null)

        //Wait for the presenter to load
        verify(mockView, timeout(5000)).fieldsEnabled = true

        //Get the entity as it would be sent to the data binding
        var entity: Person? = null
        nullableArgumentCaptor<Person> {
            verify(mockView, timeout(5000).atLeastOnce()).entity = capture()
            entity = lastValue
        }

        //Make a change to the data (using the block argument) as would be done by the view using data binding
        nullableArgumentCaptor<DoorMutableLiveData<List<PresenterFieldRow>>>() {
            verify(mockView, atLeastOnce()).presenterFieldRows = capture()
            block.invoke(lastValue!!)
            presenter.handleClickSave(entity!!)
        }

        //Wait for the save to complete
        verify(mockView, timeout(5000 * 1000)).finishWithResult(any())

        return entity!!
    }

    @Test
    fun givenNullEntity_whenUpdatedAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        val capturedPresenterFieldList = AtomicReference<List<PresenterFieldRow>>()
        createPresenterSetValsAndSave(mapOf(UstadView.ARG_ENTITY_UID to "0")) {
            capturedPresenterFieldList.set(it.getValue()!!)
            val newList = it.getValue()!!.toMutableList()
            newList[0].customFieldValue!!.customFieldValueValue = "Bob"
            it.setVal(newList)
        }

        assertEquals("Presenter sets 4 presenterfieldrows as per db",
                4, capturedPresenterFieldList.get().size)
        val fieldWithOptions = capturedPresenterFieldList.get().find { it.customFieldOptions.isNotEmpty() }
        assertEquals("Multi choice option field has 2 options", 2,
                fieldWithOptions?.customFieldOptions?.size)

        val personInDb = db.personDao.findByFirstnames("Bob")
        assertEquals("Person name found in database is the same as set by view", "Bob",
            personInDb?.firstNames)
    }

    @Test
    fun givenExistingEntity_whenUpdatedAndHandleClickSaveCalled_thenShouldUpdateInDatabase() {
        val existingPerson = Person("username", "Bob", "Jones")
        existingPerson.personUid = db.personDao.insert(existingPerson)
        createPresenterSetValsAndSave(mapOf(UstadView.ARG_ENTITY_UID to existingPerson.personUid.toString())) {
            val newList = it.getValue()!!.toMutableList()
            newList[0].customFieldValue!!.customFieldValueValue = "Joe"
            it.setVal(newList)
        }

        val personInDb = db.personDao.findByUidSync(existingPerson.personUid)
        assertEquals("Person name found in database is updated and is the", "Joe",
                personInDb?.firstNames)
    }

    @Test
    fun givenNoPicture_whenPictureUriSet_thenShouldCreateNewPersonPicture() {
        val capturedPresenterFieldList = AtomicReference<List<PresenterFieldRow>>()
        val picUri = makeTempPic()
        createPresenterSetValsAndSave(mapOf(UstadView.ARG_ENTITY_UID to "0")) {
            capturedPresenterFieldList.set(it.getValue()!!)
            val newList = it.getValue()!!.toMutableList()
            newList[0].customFieldValue!!.customFieldValueValue = "PersonWithPic"
            val picturePresenterfield = newList.find {
                it.presenterField?.fieldUid == PersonDetailPresenterField.PERSON_FIELD_UID_PICTURE.toLong()
            }

            picturePresenterfield!!.customFieldValue!!.customFieldValueValue = picUri
            it.setVal(newList)
        }

        val personInDb = db.personDao.findByFirstnames("PersonWithPic")
        val personPic = runBlocking { db.personPictureDao.findByPersonUidAsync(personInDb!!.personUid) }
        assertNotNull("Picture was created for person", personPic)
        verify(repoPersonPictureDao).insert(argThat { personPicturePersonUid == personInDb!!.personUid })
        verify(repoPersonPictureDao).setAttachment( argThat { personPicturePersonUid == personInDb!!.personUid },
            eq(picUri))

        val attachmentPath = repo.personPictureDao.getAttachmentPath(personPic!!)
        assertArrayEquals("Content of picture is as per test image",
            File(attachmentPath!!).readBytes(), File(picUri).readBytes())
    }

    @Test
    fun givenExistingPicture_whenPictureUriChanges_thenShouldUpdatePersonPicture() {
        val existingPerson = Person("username", "PersonWithPic", "Jones")
        existingPerson.personUid = db.personDao.insert(existingPerson)

        val existingPersonPicture = PersonPicture().apply {
            personPicturePersonUid = existingPerson.personUid
        }
        existingPersonPicture.personPictureUid = repo.personPictureDao.insert(existingPersonPicture)
        repo.personPictureDao.setAttachment(existingPersonPicture, makeTempPic(1))

        val newPicUri = makeTempPic()
        val capturedPresenterFieldList = AtomicReference<List<PresenterFieldRow>>()
        var capturePersonPicUri: String? = null
        createPresenterSetValsAndSave(mapOf(UstadView.ARG_ENTITY_UID to existingPerson.personUid.toString())) {
            capturedPresenterFieldList.set(it.getValue()!!)
            val newList = it.getValue()!!.toMutableList()
            val picturePresenterfield = newList.find {
                it.presenterField?.fieldUid == PersonDetailPresenterField.PERSON_FIELD_UID_PICTURE.toLong()
            }
            capturePersonPicUri = picturePresenterfield!!.customFieldValue!!.customFieldValueValue

            picturePresenterfield!!.customFieldValue!!.customFieldValueValue = newPicUri
            it.setVal(newList)
        }

        assertEquals("onLoad the presenter set the picture uri to be the uri of the attachment from DAO",
            repoPersonPictureDao.getAttachmentUri(existingPersonPicture), capturePersonPicUri)
        verify(repoPersonPictureDao).update(argThat { personPicturePersonUid == existingPerson.personUid })
        verify(repoPersonPictureDao).setAttachment( argThat { personPicturePersonUid == existingPerson.personUid },
                eq(newPicUri))
    }

    @Test
    fun givenExistingPicture_whenPictureDoesntChange_thenShouldNotUpdatePersonPicture() {
        val existingPerson = Person("username", "PersonWithPic", "Jones")
        existingPerson.personUid = db.personDao.insert(existingPerson)

        val existingPersonPicture = PersonPicture().apply {
            personPicturePersonUid = existingPerson.personUid
        }
        existingPersonPicture.personPictureUid = repo.personPictureDao.insert(existingPersonPicture)
        val tmpPicFile = makeTempPic(1)
        repo.personPictureDao.setAttachment(existingPersonPicture, tmpPicFile)

        val capturedPresenterFieldList = AtomicReference<List<PresenterFieldRow>>()
        var capturePersonPicUri: String? = null
        createPresenterSetValsAndSave(mapOf(UstadView.ARG_ENTITY_UID to existingPerson.personUid.toString())) {
            capturedPresenterFieldList.set(it.getValue()!!)
            val newList = it.getValue()!!.toMutableList()
            val picturePresenterfield = newList.find {
                it.presenterField?.fieldUid == PersonDetailPresenterField.PERSON_FIELD_UID_PICTURE.toLong()
            }
            capturePersonPicUri = picturePresenterfield!!.customFieldValue!!.customFieldValueValue

            it.setVal(newList)
        }

        assertEquals("onLoad the presenter set the picture uri to be the uri of the attachment from DAO",
                repoPersonPictureDao.getAttachmentUri(existingPersonPicture), capturePersonPicUri)

        //Should have been called once and only once in the setup here in this method, and not again
        verify(repoPersonPictureDao, times(1)).setAttachment(any(), any())
    }





}