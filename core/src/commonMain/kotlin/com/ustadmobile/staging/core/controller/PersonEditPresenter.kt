package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView.Companion.ARG_NEW_PERSON
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.ext.dbVersionHeader
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_ADDRESS
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_ATTENDANCE
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_BIRTHDAY
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_CLASSES
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_CONFIRM_PASSWORD
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FATHER_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FATHER_NUMBER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FIRST_NAMES
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FIRST_NAMES_ALT
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FULL_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_LAST_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_LAST_NAME_ALT
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_MOTHER_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_MOTHER_NUMBER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_PASSWORD
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_PHONE_NUM
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_USERNAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_ATTENDANCE
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_BIRTHDAY
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_CLASSES
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_CONFIRM_PASSWORD
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FATHER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FATHERS_NAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FATHERS_NUMBER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FIRST_NAMES
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FIRST_NAMES_ALT
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_FULL_NAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_HOME_ADDRESS
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_LAST_NAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_LAST_NAME_ALT
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_MOTHER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_MOTHERS_NAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_MOTHERS_NUMBER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_PASSWORD
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_PHONE_NUMBER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_PROFILE
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_ROLE_ASSIGNMENTS
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_HEADING_USERNAME
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_HEADER
import com.ustadmobile.lib.util.encryptPassword
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * PersonEditPresenter : This is responsible for generating the Edit data along with its Custom
 * Fields. It is also responsible for updating the data and checking for changes and handling
 * Done with Save or Discard.
 *
 * * Presenter's constructor where we are getting arguments and setting the newly/editable
 * personUid
 *
 * @param context Android context
 * @param arguments Arguments from the Activity passed here.
 * @param view  The view that called this presenter (PersonEditView->PersonEditActivity)
 */
class PersonEditPresenter (context: Any, arguments: Map<String, String>?, view: PersonEditView,
                           val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    :UstadBaseController<PersonEditView>(context, arguments!!, view) {


    private var personLiveData: DoorLiveData<Person?>? = null

    //Headers and Fields
    private var headersAndFields: List<PersonDetailPresenterField>? = null

    var personUid: Long = 0

    private var updatedPerson: Person? = null
    private var currentPerson: Person? = null
    private var currentPersonAuth: PersonAuth? = null

    private var assignedClazzes: DataSource.Factory<Int, ClazzWithNumStudents>? = null
    private var assignedRoleAssignments: DataSource.Factory<Int, EntityRoleWithGroupName>?= null
    private var groupUmLiveData: DoorLiveData<List<PersonGroup>>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    internal var database = UmAccountManager.getActiveDatabase(context)

    private var newPersonString = ""

    private val customFieldsToUpdate: MutableList<PersonCustomFieldValue>

    private var loggedInPersonUid: Long? = 0L

    private val viewIdToCustomFieldUid: HashMap<Int, Long>

    private val personDao : PersonDao
    private val personDaoDB : PersonDao
    private val personGroupDao : PersonGroupDao
    private val personGroupDaoDB : PersonGroupDao
    private var customFieldDao: CustomFieldDao
    private var customFieldValueDao: CustomFieldValueDao
    private var customFieldDaoDB: CustomFieldDao
    private var customFieldValueDaoDB: CustomFieldValueDao
    private var optionDao: CustomFieldValueOptionDao
    private var optionDaoDB: CustomFieldValueOptionDao
    private var personPictureDaoRepo : PersonPictureDao
    private val personAuthDao: PersonAuthDao
    private val personAuthDaoDB: PersonAuthDao
    private val personPictureDaoDB : PersonPictureDao
    private val personCustomFieldValueDao : PersonCustomFieldValueDao
    private val personCustomFieldValueDaoDB : PersonCustomFieldValueDao
    private val fieldsDaoRepo : PersonDetailPresenterFieldDao
    private val fieldsDaoDB : PersonDetailPresenterFieldDao
    private val feedEntryDao : FeedEntryDao
    private val feedEntryDaoDB : FeedEntryDao

    private val customFieldDropDownOptions: HashMap<Long, List<String>>

    var passwordSet: String? = null
    var confirmPasswordSet: String? = null
    var usernameSet: String? = null


    private var groupIdToPosition: HashMap<Long, Int>? = null
    private val groupPositionToId: HashMap<Int, Long>
    private var groupPresets: Array<String>? = null
    private var personWEGroupUid : Long = 0L


    init {

        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            personUid = (arguments.get(ARG_PERSON_UID)!!.toString()).toLong()
        }

        if (arguments.containsKey(ARG_NEW_PERSON)) {
            newPersonString = arguments.get(ARG_NEW_PERSON)!!.toString()
        }

        //Goldozi:
        groupIdToPosition = HashMap()
        groupPositionToId = HashMap()

        customFieldsToUpdate = ArrayList()
        viewIdToCustomFieldUid = HashMap()
        customFieldDropDownOptions = HashMap()

        personPictureDaoRepo = UmAccountManager.getRepositoryForActiveAccount(context).personPictureDao
        customFieldDaoDB = database.customFieldDao
        optionDaoDB = database.customFieldValueOptionDao
        customFieldValueDaoDB = database.customFieldValueDao
        fieldsDaoDB = database.personDetailPresenterFieldDao
        personPictureDaoDB = database.personPictureDao
        personDaoDB = database.personDao
        personGroupDaoDB = database.personGroupDao
        personCustomFieldValueDaoDB = database.personCustomFieldValueDao
        feedEntryDaoDB = database.feedEntryDao
        personAuthDaoDB = database.personAuthDao

        personAuthDao = repository.personAuthDao
        customFieldDao = repository.customFieldDao
        customFieldValueDao = repository.customFieldValueDao
        optionDao = repository.customFieldValueOptionDao
        fieldsDaoRepo = repository.personDetailPresenterFieldDao
        personDao = repository.personDao
        personGroupDao = repository.personGroupDao
        personCustomFieldValueDao = repository.personCustomFieldValueDao
        feedEntryDao = repository.feedEntryDao

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

    }

    /**
     * Presenter's Overridden onCreate that: Gets the mPerson LiveData and observe it.
     * @param savedState    The saved state
     */
    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        val personDetailPresenterFieldDao = repository.personDetailPresenterFieldDao

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        if (newPersonString == "true") {
            view.updateToolbarTitle(impl.getString(MessageID.new_person, context))
        }

        getAllPersonCustomFields()

        if(arguments.containsKey(ClazzDetailEnrollStudentView.ARG_NEW_PERSON_TYPE)){
            val t = arguments.get(ClazzDetailEnrollStudentView.ARG_NEW_PERSON_TYPE).toString()
            if(t != null && t.isNotEmpty()){
                view.setEnrollToClass(true)
            }
        }


        val thisP = this
        //Get all the currently set headers and fields:
        GlobalScope.launch {
            val resultLive =
                    personDetailPresenterFieldDao.findAllPersonDetailPresenterFieldsEditModeLive()

            GlobalScope.launch(Dispatchers.Main){
                resultLive.observeWithPresenter(thisP, thisP::handleFieldsLive)
            }
        }

        if(personUid != 0L){
            GlobalScope.launch {
                val result = personDaoDB.findByUidAsync(personUid)
                currentPerson = result
                usernameSet = currentPerson!!.username

                val result2 = personAuthDaoDB.findByUidAsync(personUid)
                currentPersonAuth = result2
                if (result2 == null) {
                    currentPersonAuth = PersonAuth()
                    currentPersonAuth!!.personAuthUid = personUid
                    currentPersonAuth!!.personAuthStatus = (PersonAuth.STATUS_NOT_SENT)
                }
            }
        }
    }

    private fun handleFieldsLive(fields: List<PersonDetailPresenterField>?){
        val cleanedResult = ArrayList<PersonDetailPresenterField>()
        //Remove old custom fields
        val fieldsIterator = fields!!.iterator()
        while (fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            cleanedResult.add(field)
        }

        headersAndFields = cleanedResult

        //Get person live data and observe
        personLiveData = personDao.findByUidLive(personUid)
        //Observe the live data
        view.runOnUiThread(Runnable {
            personLiveData!!.observeWithPresenter(this, this::handlePersonValueChanged)
        })
    }

    /**
     * This method tells the View what to show. It will set every field item to the view.
     * The Live Data handler calls this method when the data (via Live data) is updated.
     *
     * @param person The person that needs to be displayed.
     */
    private fun handlePersonValueChanged(person: Person?) {

        if (updatedPerson == null || updatedPerson != person) {
            //set fields on the view as they change and arrive.
            if (person != null) {
                setFieldsOnView(person, headersAndFields!!, view)
                updatedPerson = person
            }
        }
    }


    fun addToMap(viewId: Int, fieldId: Long) {
        viewIdToCustomFieldUid[viewId] = fieldId
    }

    /**
     * Getting custom fields (new way)
     */
    private fun getAllPersonCustomFields() {
        //0. Clear all added custom fields on view.
        view.runOnUiThread(Runnable{ view.clearAllCustomFields() })

        GlobalScope.launch {
            //1. Get all custom fields
            val result = customFieldDaoDB!!.findAllCustomFieldsProviderForEntityAsync(Person.TABLE_ID)
            for (c in result) {
                //Get value as well
                val result2 = customFieldValueDaoDB!!.findValueByCustomFieldUidAndEntityUid(
                        c.customFieldUid, personUid)
                var valueString: String? = ""
                var valueSelection = 0

                if (c.customFieldType == CustomField.FIELD_TYPE_TEXT) {

                    if (result2 != null) {
                        valueString = result2.customFieldValueValue
                    }
                    val finalValueString = arrayOf<String>(valueString!!)
                    view.runOnUiThread(Runnable{

                        view.addCustomFieldText(c, finalValueString[0])
                    })

                } else if (c.customFieldType == CustomField.FIELD_TYPE_DROPDOWN) {
                    if (result2 != null) {
                        try {
                            valueSelection = (result2.customFieldValueValue!!).toInt()
                        } catch (nfe: NumberFormatException) {
                            valueSelection = 0
                        }

                    }
                    val finalValueSelection = valueSelection
                    val result3 = optionDaoDB!!.findAllOptionsForFieldAsync(c.customFieldUid)
                    val options = ArrayList<String>()

                    for (o in result3!!) {
                        options.add(o.customFieldValueOptionName!!)
                    }
                    //Get value
                    var valueString = "-"
                    if (finalValueSelection > 0) {
                        valueString = options[finalValueSelection]
                    }
                    val finalValueString = valueString

                    customFieldDropDownOptions[c.customFieldUid] = options
                    view.runOnUiThread(Runnable{
                        var a = arrayOfNulls<String>(options.size)
                        a = options.toTypedArray()
                        view.addCustomFieldDropdown(c, a, finalValueSelection)
                    })
                }
            }
        }
    }


    fun generateGroupList(){
        //Update group
        groupUmLiveData = personGroupDao.findAllActiveGroupPersonGroupsLive()
        view.runOnUiThread(Runnable {
            groupUmLiveData!!.observeWithPresenter(this, this::handleAllGroupsChanged)
        })
    }

    private fun handleAllGroupsChanged(groups: List<PersonGroup>?) {
        var selectedPosition = 0

        val entityList = ArrayList<String>()
        groupIdToPosition = HashMap()

        groupIdToPosition!![0] = 0
        groupPositionToId[0] = 0
        entityList.add(impl.getString(MessageID.no_women_embroiderers_set, context))

        var posIter = 1
        for (everyEntity in groups!!) {
            entityList.add(everyEntity.groupName!!)
            groupIdToPosition!![everyEntity.groupUid] = posIter
            groupPositionToId[posIter] = everyEntity.groupUid
            posIter++
        }
        groupPresets = entityList.toTypedArray()

        if (personWEGroupUid != 0L) {

            if (groupIdToPosition!!.containsKey(personWEGroupUid)) {
                selectedPosition = groupIdToPosition!![personWEGroupUid]!!
            }
        }

        view.setGroupPresets(groupPresets!!, selectedPosition)
    }


    fun updateGroup(position: Int) {

        if(position > 0) {
            if (groupPositionToId.containsKey(position))
                personWEGroupUid = groupPositionToId[position]!!
        }else{
            personWEGroupUid = 0
        }
    }

    /**
     * Updates the pic of the person after taken to the Person object directly
     *
     * @param imageFilePath    The whole path of the picture.
     */
    fun updatePersonPic(imageFilePath: String) {


        //Find the person
        GlobalScope.launch {
            val thisPerson = personDao.findByUidAsync(personUid)

            var personPictureUid : Long = 0L
            var existingPP: PersonPicture ? = null
            existingPP = personPictureDaoRepo.findByPersonUidAsync(personUid)
            if(existingPP == null){
                existingPP = PersonPicture()
                existingPP.personPicturePersonUid = personUid
                existingPP.picTimestamp = UMCalendarUtil.getDateInMilliPlusDays(0)
                personPictureUid = personPictureDaoRepo.insertAsync(existingPP)
                existingPP.personPictureUid = personPictureUid
            }

            personPictureDaoRepo.setAttachment(existingPP, imageFilePath)
            existingPP.picTimestamp = UMCalendarUtil.getDateInMilliPlusDays(0)
            personPictureDaoRepo.update(existingPP)

            //Update personWithpic
            personDao.updatePersonAsync(thisPerson!!, loggedInPersonUid!!)
            generateFeedsForPersonUpdate(repository, database, updatedPerson!!)

        }
    }

    /**
     * Generates live data for Clazz list to be assigned to the current Person being edited.
     */
    fun generateAssignedClazzesLiveData() {
        val clazzDao = repository.clazzDao
        assignedClazzes = clazzDao.findAllClazzesByPersonUid(personUid)
        updateClazzListProviderToView()
    }

    fun generateAssignedRoleAssignments(){
        val roleAssignmentDao = repository.entityRoleDao
        assignedRoleAssignments =
                roleAssignmentDao.findAllActiveRoleAssignmentsByGroupPersonUid(personUid)
        setRoleAssignmentsOnView()
    }

    /**
     * Updates the Clazz List provider of type ClazzWithNumStudents that is set on this Presenter to
     * the View.
     */
    private fun updateClazzListProviderToView() {
        view.setClazzListProvider(assignedClazzes!!)
    }

    private fun setRoleAssignmentsOnView(){
        view.setRoleAssignmentListProvider(assignedRoleAssignments!!)
    }

    private fun updatePersonPic(thisPerson: Person) {
        GlobalScope.launch {
            //Load the local image first
            val personPictureLocal = personPictureDaoDB.findByPersonUidAsync(
                    thisPerson!!.personUid)
            if(personPictureLocal != null) {
                val imagePathLocal = personPictureDaoRepo.getAttachmentPath(personPictureLocal!!)!!;

                if (imagePathLocal.isNotEmpty())
                    view.updateImageOnView(imagePathLocal)
            }

            //Get the server image
            val personPictureServer =
                    personPictureDaoRepo.findByPersonUidAsync(thisPerson!!.personUid)
            if(personPictureServer != null) {
                val imagePathServer =
                        personPictureDaoRepo.getAttachmentPath(personPictureServer!!)!!;

                if (imagePathServer.isNotEmpty())
                    view.updateImageOnView(imagePathServer)
            }
        }
    }

    /**
     * Common method to set edit fields up for the current Person Editing.
     *
     * @param thisPerson The person being edited
     * @param allFields The Fields
     * @param thisView  The View
     * @param valueMap  The Custom fields value map
     */
    private fun setFieldsOnView(thisPerson: Person, allFields: List<PersonDetailPresenterField>,
                                thisView: PersonEditView) {

        //TODO: Locale on Kotlin Core
//        val currnetLocale = Locale.getDefault()
        val currnetLocale = ""

        updatePersonPic(thisPerson)

        //Clear all view before setting fields ?
        view.clearAllFields()

        for (field in allFields) {

            var labelMessageId = 0
            when(field.labelMessageId){
                FIELD_HEADING_PROFILE -> { labelMessageId = MessageID.profile }
                FIELD_HEADING_FULL_NAME -> { labelMessageId = MessageID.field_fullname }
                FIELD_HEADING_FIRST_NAMES -> { labelMessageId = MessageID.first_names }
                FIELD_HEADING_LAST_NAME -> { labelMessageId = MessageID.last_name }
                FIELD_HEADING_FIRST_NAMES_ALT -> { labelMessageId = MessageID.field_first_names_alternative }
                FIELD_HEADING_LAST_NAME_ALT -> { labelMessageId = MessageID.last_name_alternative }
                FIELD_HEADING_BIRTHDAY -> { labelMessageId = MessageID.birthday }
                FIELD_HEADING_HOME_ADDRESS -> { labelMessageId = MessageID.home_address }
                FIELD_HEADING_ATTENDANCE -> { labelMessageId = MessageID. attendance}
                FIELD_HEADING_FATHER -> { labelMessageId = MessageID.father }
                FIELD_HEADING_FATHERS_NAME -> { labelMessageId = MessageID.fathers_name }
                FIELD_HEADING_FATHERS_NUMBER -> { labelMessageId = MessageID.fathers_number }
                FIELD_HEADING_MOTHERS_NAME -> { labelMessageId = MessageID.mothers_name }
                FIELD_HEADING_MOTHERS_NUMBER -> { labelMessageId = MessageID.mothers_number }
                FIELD_HEADING_MOTHER -> { labelMessageId = MessageID.mother }
                FIELD_HEADING_CLASSES -> { labelMessageId = MessageID.classes }
                FIELD_HEADING_USERNAME -> { labelMessageId = MessageID.username }
                FIELD_HEADING_PHONE_NUMBER -> { labelMessageId = MessageID.phone_number}
                FIELD_HEADING_PASSWORD -> {
                    if (newPersonString == "true") {
                        labelMessageId = MessageID.password
                    }else {
                        labelMessageId = MessageID.password_unchanged
                    }
                }
                FIELD_HEADING_CONFIRM_PASSWORD -> {
                    if (newPersonString == "true") {
                        labelMessageId = MessageID.confirm_password
                    }else {
                        labelMessageId = MessageID.confirm_password_unchanged
                    }
                }
            }

            var headerMessageId = 0
            when(field.headerMessageId){
                FIELD_HEADING_PROFILE -> { headerMessageId = MessageID.profile }
                FIELD_HEADING_FULL_NAME -> { headerMessageId = MessageID.field_fullname }
                FIELD_HEADING_FIRST_NAMES -> { headerMessageId = MessageID.first_names }
                FIELD_HEADING_LAST_NAME -> { headerMessageId = MessageID.last_name }
                FIELD_HEADING_FIRST_NAMES_ALT -> { labelMessageId = MessageID.field_first_names_alternative }
                FIELD_HEADING_LAST_NAME_ALT -> { labelMessageId = MessageID.last_name_alternative }
                FIELD_HEADING_BIRTHDAY -> { headerMessageId = MessageID.birthday }
                FIELD_HEADING_HOME_ADDRESS -> { headerMessageId = MessageID.home_address }
                FIELD_HEADING_ATTENDANCE -> { headerMessageId = MessageID. attendance}
                FIELD_HEADING_FATHER -> { headerMessageId = MessageID.father }
                FIELD_HEADING_FATHERS_NAME -> { headerMessageId = MessageID.fathers_name }
                FIELD_HEADING_FATHERS_NUMBER -> { headerMessageId = MessageID.fathers_number }
                FIELD_HEADING_MOTHERS_NAME -> { headerMessageId = MessageID.mothers_name }
                FIELD_HEADING_MOTHERS_NUMBER -> { headerMessageId = MessageID.mothers_number }
                FIELD_HEADING_MOTHER -> { headerMessageId = MessageID.mother }
                FIELD_HEADING_CLASSES -> { headerMessageId = MessageID.classes }
                FIELD_HEADING_USERNAME -> { headerMessageId = MessageID.username }
                FIELD_HEADING_PHONE_NUMBER -> { headerMessageId = MessageID.phone_number }

                FIELD_HEADING_PASSWORD -> {
                    if (newPersonString == "true") {
                        headerMessageId = MessageID.password
                    }else {
                        headerMessageId = MessageID.password_unchanged
                    }
                }
                FIELD_HEADING_CONFIRM_PASSWORD -> {
                    if (newPersonString == "true") {
                        headerMessageId = MessageID.confirm_password
                    }else {
                        headerMessageId = MessageID.confirm_password_unchanged
                    }
                }
                FIELD_HEADING_ROLE_ASSIGNMENTS -> { headerMessageId = MessageID.role_assignments }
            }

            var thisValue: String? = ""

            if (field.fieldType == FIELD_TYPE_HEADER) {
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(FIELD_TYPE_HEADER,
                                headerMessageId, null), headerMessageId)
                continue
            }

            if (field.fieldUid == PERSON_FIELD_UID_FULL_NAME.toLong()) {
                thisValue = thisPerson.firstNames + " " + thisPerson.lastName
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_FIRST_NAMES.toLong()) {
                thisValue = thisPerson.firstNames
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_LAST_NAME.toLong()) {
                thisValue = thisPerson.lastName
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            }

            else if (field.fieldUid == PERSON_FIELD_UID_FIRST_NAMES_ALT.toLong()) {
                thisValue = thisPerson.personFirstNamesAlt
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_LAST_NAME_ALT.toLong()) {
                thisValue = thisPerson.personLastNameAlt
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            }

            else if (field.fieldUid == PERSON_FIELD_UID_PHONE_NUM.toLong()) {
                thisValue = thisPerson.phoneNum
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            }

            else if (field.fieldUid == PERSON_FIELD_UID_ATTENDANCE.toLong()) {
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_CLASSES.toLong()) {
                thisValue = "Class Name ..."
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER.toLong()) {
                thisValue = thisPerson.fatherName + " (" + thisPerson.fatherNumber + ")"
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)

            } else if (field.fieldUid == PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER.toLong()) {
                thisValue = thisPerson.motherName + " (" + thisPerson.motherNum + ")"
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_FATHER_NAME.toLong()) {
                thisValue = thisPerson.fatherName
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_MOTHER_NAME.toLong()) {
                thisValue = thisPerson.motherName
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_FATHER_NUMBER.toLong()) {
                thisValue = thisPerson.fatherNumber
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_MOTHER_NUMBER.toLong()) {
                thisValue = thisPerson.motherNum
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_ADDRESS.toLong()) {
                thisValue = thisPerson.personAddress
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_BIRTHDAY.toLong()) {
                if(thisPerson.dateOfBirth > 0L) {
                    thisValue = UMCalendarUtil.getPrettyDateFromLong(
                            thisPerson.dateOfBirth, currnetLocale)
                }else{
                    thisValue = ""
                }
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if (field.fieldUid == PERSON_FIELD_UID_USERNAME.toLong() ) {
                usernameSet = thisPerson.username
                thisValue = usernameSet

                if(thisValue == null){
                    thisValue = ""
                }
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else if(field.fieldUid == PERSON_FIELD_UID_PASSWORD.toLong()
                    || field.fieldUid == PERSON_FIELD_UID_CONFIRM_PASSWORD.toLong()){
                thisValue = ""
                thisView.setField(field.fieldIndex, field.fieldUid,
                        PersonDetailViewField(field.fieldType,
                                labelMessageId, field.fieldIcon), thisValue)
            } else {//unknown field
                var messageLabel = 0
                var iconName: String? = null
                var fieldValue: String? = null
            }
        }
    }

    /**
     * Updates fields of Person w.r.t field Id given and the value. This method does NOT persist
     * the data.
     *
     * @param personToUpdate the Person object to update values for.
     * @param fieldcode The field Uid that needs to get updated
     * @param value The value to update the Person's field.
     * @return  The updated Person with the updated field.
     */
    private fun updateSansPersistPersonField(personToUpdate: Person?,
                                             fieldcode: Long, value: Any): Person {

        //Update Core fields
        if (fieldcode == PERSON_FIELD_UID_FIRST_NAMES.toLong()) {
            personToUpdate!!.firstNames = value as String

        } else if (fieldcode == PERSON_FIELD_UID_LAST_NAME.toLong()) {
            personToUpdate!!.lastName = value as String
        }

        else if (fieldcode == PERSON_FIELD_UID_FIRST_NAMES_ALT.toLong()) {
            personToUpdate!!.personFirstNamesAlt = value as String

        } else if (fieldcode == PERSON_FIELD_UID_LAST_NAME_ALT.toLong()) {
            personToUpdate!!.personLastNameAlt = value as String
        } else if (fieldcode == PERSON_FIELD_UID_PHONE_NUM.toLong()){
            personToUpdate!!.phoneNum = value as String
        }

        else if (fieldcode == PERSON_FIELD_UID_FATHER_NAME.toLong()) {
            personToUpdate!!.fatherName = (value as String)

        } else if (fieldcode == PERSON_FIELD_UID_FATHER_NUMBER.toLong()) {
            personToUpdate!!.fatherNumber = (value as String)

        } else if (fieldcode == PERSON_FIELD_UID_MOTHER_NAME.toLong()) {
            personToUpdate!!.motherName = (value as String)

        } else if (fieldcode == PERSON_FIELD_UID_MOTHER_NUMBER.toLong()) {
            personToUpdate!!.motherName = (value as String)

        } else if (fieldcode == PERSON_FIELD_UID_BIRTHDAY.toLong()) {
            personToUpdate!!.dateOfBirth = (value as Long)

        } else if (fieldcode == PERSON_FIELD_UID_ADDRESS.toLong()) {
            personToUpdate!!.personAddress = (value as String)

        } else if (fieldcode == PERSON_FIELD_UID_USERNAME.toLong() ){
            usernameSet = value as String
            //personToUpdate!!.username = usernameSet
        } else if (fieldcode == PERSON_FIELD_UID_PASSWORD.toLong() ){
            passwordSet = value as String
        } else if (fieldcode == PERSON_FIELD_UID_CONFIRM_PASSWORD.toLong() ){
            confirmPasswordSet = value as String
        }else {
            //This is actually a custom field. (old)
            GlobalScope.launch {
                val result = personCustomFieldValueDaoDB
                        .findCustomFieldByFieldAndPersonAsync(fieldcode, personToUpdate!!.personUid)
                if (result != null) {
                    result.fieldValue = (value.toString())
                    customFieldsToUpdate.add(result)
                } else {
                    //Create the custom field
                    val newCustomValue = PersonCustomFieldValue()
                    newCustomValue.personCustomFieldValuePersonUid = (personToUpdate.personUid)
                    newCustomValue.personCustomFieldValuePersonCustomFieldUid = (fieldcode)
                    personCustomFieldValueDao.insert(newCustomValue)
                    newCustomValue.fieldValue = (value.toString())
                    customFieldsToUpdate.add(newCustomValue)
                }
            }
        }

        return personToUpdate!!

    }

    /**
     * Handles every field Edit (focus changed).
     *
     * @param fieldCode The field code that needs editing
     * @param value The new value of the field from the view
     */
    fun handleFieldEdited(fieldCode: Long, value: Any) {

        updatedPerson = updateSansPersistPersonField(updatedPerson, fieldCode, value)
    }

    /**
     * Click handler when Add new Class clicked on Classes section
     */
    fun handleClickAddNewClazz() {
        val args = HashMap<String, String>()
        args.put(ARG_PERSON_UID, personUid.toString())
        impl.go(PersonDetailEnrollClazzView.VIEW_NAME, args, context)
    }

    fun handleClickAddNewRoleAssignment(){
        val args = HashMap<String, String>()
        args.put(ARG_PERSON_UID, personUid.toString())
        impl.go(RoleAssignmentDetailView.VIEW_NAME, args, context)
    }

    /**
     * Saves custom field values.
     * @param viewId
     * @param type
     * @param value
     */
    fun handleSaveCustomFieldValues(viewId: Int, type: Int, value: Any) {

        //Lookup viewId
        if (viewIdToCustomFieldUid.containsKey(viewId)) {
            val customFieldUid = viewIdToCustomFieldUid[viewId]!!

            var valueString: String? = null
            if (type == CustomField.FIELD_TYPE_TEXT) {
                valueString = value.toString()

            } else if (type == CustomField.FIELD_TYPE_DROPDOWN) {
                val spinnerSelection = value as Int
                val options = customFieldDropDownOptions[customFieldUid]
                valueString = spinnerSelection.toString()
            }

            if (valueString != null && !valueString.isEmpty()) {
                val finalValueString = valueString
                GlobalScope.launch {
                    val result =
                            customFieldValueDao!!.findValueByCustomFieldUidAndEntityUid(
                                    customFieldUid, personUid)
                    val customFieldValue: CustomFieldValue?
                    if (result == null) {
                        customFieldValue = CustomFieldValue()
                        customFieldValue.customFieldValueEntityUid = personUid
                        customFieldValue.customFieldValueFieldUid = customFieldUid
                        customFieldValue.customFieldValueValue = finalValueString
                        customFieldValueDao!!.insert(customFieldValue)
                    } else {
                        customFieldValue = result
                        customFieldValue.customFieldValueValue = finalValueString
                        customFieldValueDao!!.update(customFieldValue)
                    }
                }
            }
        }
    }


    /**
     * Done click handler on the Edit / Enrollment page: Clicking done will persist and save it and
     * end the activity.
     *
     */
    fun handleClickDone() {


        updatedPerson!!.active = true
        updatedPerson!!.active = true
        updatedPerson!!.mPersonGroupUid = personWEGroupUid
        GlobalScope.launch {

        }
        GlobalScope.launch {
            personDao.updatePersonAsync(updatedPerson!!, loggedInPersonUid!!)

            //Update the custom fields
            personCustomFieldValueDao.updateListAsync(customFieldsToUpdate)
            //Start of feed generation
            generateFeedsForPersonUpdate(repository, database, updatedPerson!!)

            //Update person's individual group to  set the right name of the group
            val fullName = updatedPerson!!.fullName(impl.getLocale(context))
            val personGroup = personGroupDaoDB.findPersonIndividualGroup(updatedPerson!!.personUid)
            if(personGroup != null){
                personGroup.groupName = fullName + "'s individual person group"
                personGroupDao.updateAsync(personGroup)
            }

            //Update password if necessary
            updateUsernameAndPassword()
        }

    }

    private fun updateUsernameAndPassword() : Boolean {

        view.runOnUiThread(Runnable {
            view.setInProgress(true)
        })

        GlobalScope.launch {

            //Check username
            if((usernameSet!= null && currentPerson!!.username == null) || (currentPerson != null && currentPerson!!.username != null &&
                            !currentPerson!!.username.equals(usernameSet))) {
                //Update on server
                try {
                    val serverUrl = UmAccountManager.getActiveEndpoint(context)
                    val resetPasswordResponse = defaultHttpClient().get<HttpResponse>()
                    {
                        url {
                            takeFrom(serverUrl!!)
                            encodedPath = "${encodedPath}UmAppDatabase/PersonDao/isUsernameAvailable"
                        }
                        parameter("p0", usernameSet)
                        dbVersionHeader(repository)
                    }

                    if (resetPasswordResponse.status == HttpStatusCode.OK) {

                        val result = resetPasswordResponse.readText()
                        if (!result.toBoolean()) {
                            view.sendMessage(MessageID.username_not_available)
                            view.runOnUiThread(Runnable {
                                view.setInProgress(false)
                            })
                            false
                        } else {
                            updatedPerson!!.username = usernameSet
                            currentPerson!!.username = usernameSet
                            personDaoDB.updateAsync(updatedPerson!!)
                            updatePassword()
                        }
                    } else {
                        view.sendMessage(MessageID.unable_to_update_username)
                        view.runOnUiThread(Runnable {
                            view.setInProgress(false)
                        })
                        false
                    }
                } catch (e: Exception) {
                    view.sendMessage(MessageID.unable_to_update_username_error)
                    view.runOnUiThread(Runnable {
                        view.setInProgress(false)
                    })
                    false
                }
            }else{
                updatePassword()
            }
            false
        }
        return false
    }


    private fun updatePassword(): Boolean{
        view.runOnUiThread(Runnable {
            view.setInProgress(true)
        })
        GlobalScope.launch {

            if (passwordSet != null && !passwordSet!!.isEmpty() && usernameSet != null
                    && !usernameSet!!.isEmpty() && currentPersonAuth != null
                    && currentPerson != null) {

                if (passwordSet != confirmPasswordSet) {
                    view.runOnUiThread(Runnable {
                        view.setInProgress(false)
                        view.sendMessage(MessageID.passwords_dont_match)
                    })
                    false
                }else {

                    //Update locally
                    personDao.updateAsync(updatedPerson!!)

                    currentPersonAuth!!.passwordHash = PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                            encryptPassword(passwordSet!!)
                    currentPersonAuth!!.personAuthStatus = (PersonAuth.STATUS_NOT_SENT)


                    //Update on server
                    try {
                        val serverUrl = UmAccountManager.getActiveEndpoint(context)
                        val resetPasswordResponse = defaultHttpClient().get<HttpResponse>()
                        {
                            url {
                                takeFrom(serverUrl!!)
                                encodedPath = "${encodedPath}UmAppDatabase/PersonAuthDao/resetPassword"
                            }
                            parameter("p0", personUid)
                            parameter("p1", passwordSet)
                            parameter("p2", loggedInPersonUid)
                            dbVersionHeader(repository)
                        }

                        if (resetPasswordResponse.status == HttpStatusCode.OK) {
                            //Update locally
                            personAuthDao.updateAsync(currentPersonAuth!!)
                            view.runOnUiThread(Runnable {
                                view.finish()
                            })
                        } else {
                            view.sendMessage(MessageID.unable_to_update_password)
                            view.runOnUiThread(Runnable {
                                view.setInProgress(false)
                            })
                            false
                        }
                    } catch (e: Exception) {
                        view.sendMessage(MessageID.unable_to_update_password)
                        view.runOnUiThread(Runnable {
                            view.setInProgress(false)
                        })
                        false
                    }
                }
            }else{
                view.runOnUiThread(Runnable {
                    view.finish()
                })
            }

        }
        return false
    }



    companion object {

        /**
         *TODO: This is a tricky situation. We want to create the logs locally for every admin/mne/etc.
         * The problem is that we shouldn't/don't have a list of all admins & mne's on students/teachers devices.
         * This should be a job for the server. But at the same time we need the logs locally if the device is offline.
         *
         */

        internal fun generateFeedsForPersonUpdate(repository: UmAppDatabase,
                                                  database: UmAppDatabase, mUpdatedPerson: Person) {

            val feedEntryDao = repository.feedEntryDao
            val feedEntryDaoDB = database.feedEntryDao
            val clazzDao = repository.clazzDao
            val clazzDaoDB = database.clazzDao
            val roleDao = repository.roleDao
            val roleDaoDB = database.roleDao
            val personDao = repository.personDao
            val personDaoDB = database.personDao

            //All edits trigger a feed
            val personClazzes = clazzDaoDB.findAllClazzesByPersonUidAsList(mUpdatedPerson.personUid)

            val newFeedEntries = ArrayList<FeedEntry>()
            val updateFeedEntries = ArrayList<FeedEntry>()

            val feedLinkViewPerson = PersonDetailView.VIEW_NAME + "?" + ARG_PERSON_UID + "=" +
                    mUpdatedPerson.personUid

            for (everyClazz in personClazzes) {
                //Find officers and admins
                val mneOfficerRole = roleDaoDB.findByNameSync(Role.ROLE_NAME_MNE)
                val mneofficers = clazzDaoDB.findPeopleWithRoleAssignedToClazz(
                        everyClazz.clazzUid, mneOfficerRole!!.roleUid)
                val admins = personDaoDB.findAllAdminsAsList()

                //For every officer,create feed entry
                for (mne in mneofficers) {
                    val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                            mne.personUid, everyClazz.clazzUid,
                            ScheduledCheck.TYPE_CHECK_PERSON_PROFILE_UPDATED,
                            feedLinkViewPerson)

                    val thisEntry = FeedEntry(
                            feedEntryUid,
                            "Student details updated",
                            "Student " + mUpdatedPerson.firstNames
                                    + " " + mUpdatedPerson.lastName + " details updated",
                            feedLinkViewPerson, everyClazz.clazzName!!, mne.personUid
                    )
                    thisEntry.dateCreated = UMCalendarUtil.getDateInMilliPlusDays(0)
                    val existingEntry = feedEntryDaoDB.findByUid(feedEntryUid)

                    if (existingEntry == null) {
                        newFeedEntries.add(thisEntry)
                    } else {
                        updateFeedEntries.add(thisEntry)
                    }
                }

                //For every admin, create feed entry.
                for (admin in admins) {
                    val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                            admin.personUid, everyClazz.clazzUid,
                            ScheduledCheck.TYPE_CHECK_PERSON_PROFILE_UPDATED,
                            feedLinkViewPerson)

                    val thisEntry = FeedEntry(
                            feedEntryUid,
                            "Student details updated",
                            "Student " + mUpdatedPerson.firstNames
                                    + " " + mUpdatedPerson.lastName + " details updated",
                            feedLinkViewPerson, everyClazz.clazzName!!, admin.personUid
                    )
                    thisEntry.dateCreated = UMCalendarUtil.getDateInMilliPlusDays(0)
                    val existingEntry = feedEntryDaoDB.findByUid(feedEntryUid)

                    if (existingEntry == null) {
                        newFeedEntries.add(thisEntry)
                    } else {
                        updateFeedEntries.add(thisEntry)
                    }
                }
            }

            feedEntryDao.insertList(newFeedEntries)
            feedEntryDao.updateList(updateFeedEntries)

            //End of feed Generation
        }
    }


}
