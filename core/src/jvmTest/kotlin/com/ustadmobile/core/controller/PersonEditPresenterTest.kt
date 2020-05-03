package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.TYPE_FIELD
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

class PersonEditPresenterTest {

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private val context = Any()

    private lateinit var systemImpl: UstadMobileSystemImpl

    private lateinit var personDetailPresenterFieldFirstNames: PersonDetailPresenterField

    private lateinit var personDetailPresenterFieldCustomId: PersonDetailPresenterField

    private lateinit var personDetailPresenterFieldCustomDropdown: PersonDetailPresenterField

    private lateinit var customIdField: CustomField

    private lateinit var customDropDownField: CustomField

    private lateinit var customDropDownFieldOptions: List<CustomFieldValueOption>

    private lateinit var mockView: PersonEditView

    @Before
    fun setup() {
        checkJndiSetup()
        db = UmAppDatabase.getInstance(context)
        repo = db
        db.clearAllTables()

        personDetailPresenterFieldFirstNames = PersonDetailPresenterField(
                fieldUid = PersonDetailPresenterField.PERSON_FIELD_UID_FIRST_NAMES.toLong(),
                fieldType = TYPE_FIELD, fieldIndex = 0)
        personDetailPresenterFieldCustomId = PersonDetailPresenterField(
                fieldUid = 1042L, fieldType = TYPE_FIELD, fieldIndex = 1)
        personDetailPresenterFieldCustomDropdown = PersonDetailPresenterField(
                fieldUid = 1043L, fieldType = TYPE_FIELD, fieldIndex = 2)

        customIdField = CustomField(1042L, customFieldName = "Our Custom ID")
        customDropDownField = CustomField(1043L, customFieldName = "Custom Dropdown")
        db.personDetailPresenterFieldDao.insertList(listOf(personDetailPresenterFieldFirstNames,
                personDetailPresenterFieldCustomId, personDetailPresenterFieldCustomDropdown))
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

    fun createPresenterSetValsAndSave(args: Map<String, String>, block: (DoorMutableLiveData<List<PresenterFieldRow>>) -> Unit): Person {
        val mockLifecycle = mock<DoorLifecycleOwner> {}
        val presenter = PersonEditPresenter(context, args, mockView, mockLifecycle,
                systemImpl, db, repo, UmAccountManager.activeAccountLiveData)
        presenter.onCreate(null)

        //Wait for the presenter to load
        verify(mockView, timeout(5000)).presenterFieldRows = argThat { getValue()?.isNotEmpty() ?: false}

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
        verify(mockView, timeout(5000)).finishWithResult(any())

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

        assertEquals("Presenter sets 3 presenterfieldrows as per db",
                3, capturedPresenterFieldList.get().size)
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


}