package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.BulkUploadMasterView
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class BulkUploadMasterPresenter(context: Any, arguments: Map<String, String>?,
                                view: BulkUploadMasterView) :
        UstadBaseController<BulkUploadMasterView>(context, arguments!!, view) {

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    private var currentPosition = 0
    var lines: List<String>? = null
    private var teacherRoleUid = 0L
    private var chosenTZ: String? = null
    private var allTZ: List<String>? = null

    private val locationDao: LocationDao
    private val roleDao: RoleDao
    private val clazzDao: ClazzDao
    private val clazzMemberDao: ClazzMemberDao
    private val personDao: PersonDao
    private val customFieldDao: CustomFieldDao
    private val customFieldValueDao: CustomFieldValueDao
    private val personGroupMemberDao: PersonGroupMemberDao
    private val entityRoleDao: EntityRoleDao
    private val personFieldDao: PersonCustomFieldDao
    private val personCustomFieldValueDao: PersonCustomFieldValueDao

    private var thereWasAnError = false

    internal lateinit var bulkLine: BulkUploadLine

    init {
        locationDao = repository.locationDao
        roleDao = repository.roleDao
        clazzDao = repository.clazzDao
        clazzMemberDao = repository.clazzMemberDao
        personDao = repository.personDao
        personGroupMemberDao = repository.personGroupMemberDao
        entityRoleDao = repository.entityRoleDao
        customFieldDao = repository.customFieldDao
        customFieldValueDao = repository.customFieldValueDao
        personFieldDao = repository.personCustomFieldDao
        personCustomFieldValueDao = repository.personCustomFieldValueDao
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //TODO: KMP Fix when TimeZone is working
        //Get all time zones as list of string and set it on the view.
//        allTZ = Arrays.asList(*TimeZone.getAvailableIDs())
        allTZ = emptyList()
        view.setTimeZonesList(allTZ!!)
        //Set choosen Timezone as default timezone
//        chosenTZ = TimeZone.getDefault().getID()
        chosenTZ = ""

        //Get Teacher role
        getRole()

    }

    /**
     * Update chosen timezone
     */
    fun setTimeZoneSelected(position: Int, id: Long) {
        chosenTZ = allTZ!![position]
    }

    fun startParsing() {
        //Start with header line at position 0:
        val headerLine = lines!![0]
        bulkLine = BulkUploadLine()
        bulkLine.processHeader(headerLine)

        //Continue with next line data.
        processNextLine()

    }

    /**
     * Start processing the next line
     */
    private fun processNextLine() {
        currentPosition++
        println("BULK UPLOAD LINE : $currentPosition")

        view.updateProgressValue(currentPosition, lines!!.size)

        if (currentPosition >= lines!!.size) {
            //If at the end of the line, finish the activity.
            if (view.getAllErrors()!!.size > 0) {
                //Don't finish activity yet. Remain to show errors.
                if (thereWasAnError) {
                    view.setErrorHeading(MessageID.please_review_errors)
                } else {
                    view.setErrorHeading(MessageID.please_review_warnings)
                }

            } else {
                view.finish()
            }
        } else {
            //Get the line and Start processing it.
            parseLine(lines!![currentPosition])
        }

    }


    /**
     * Every line to be parsed contains all the information including Student, Tecaher, Class,
     * Location, etc. This is why we need to run things in order.
     * We need to set up in a way where we first make sure we get all Roles and Daos required.
     * Then we need to process Location. Then we need proces Class, then Teachers and then students.
     *
     * @param line  The bulk upload line
     */
    private fun parseLine(line: String) {
        bulkLine.setLine(line)
        processLocations(bulkLine)
    }

    /**
     * Process the 4 levels of locations in the bulk upload line.
     * @param bulkLine  The line
     */
    private fun processLocations(bulkLine: BulkUploadLine) {

        val locationGovernorateTitle = bulkLine.location1
        val locationDistrictTitle = bulkLine.location2
        val locationTownTitle = bulkLine.location3
        val locationLeafTitle = bulkLine.locationLeaf

        val locationGovernorate = Location()
        locationGovernorate.title = locationGovernorateTitle
        locationGovernorate.timeZone = (chosenTZ)

        val locationDistrict = Location()
        locationDistrict.title = locationDistrictTitle
        locationDistrict.timeZone = (chosenTZ)

        val locationTown = Location()
        locationTown.title = locationTownTitle
        locationTown.timeZone = (chosenTZ)

        val locationLeaf = Location()
        locationLeaf.title = locationLeafTitle
        locationLeaf.timeZone = (chosenTZ)

        GlobalScope.launch {
            val location1s = locationDao.findByTitleAsync(locationGovernorateTitle)

            var move = false
            if (location1s!!.size == 0) {
                //Location not created. Create it.
                locationGovernorate.locationUid = locationDao.insert(locationGovernorate)
                move = true
            } else if (location1s.size == 1) {
                //Location already exists. Getting uid.
                locationGovernorate.locationUid = location1s[0].locationUid
                move = true
            } else {
                view.addError("WARNING: More than one instances of " + locationGovernorateTitle +
                        " location. Please delete duplicate Locations.",
                        false)
                locationGovernorate.locationUid = location1s[0].locationUid
                move = true
            }

            if (move) {
                val location2s = locationDao.findByTitle(locationDistrictTitle)
                var move2 = false
                if (location2s.size == 0) {
                    move2 = true
                    //Location not created. Create it.
                    locationDistrict.parentLocationUid = locationGovernorate.locationUid
                    locationDistrict.locationUid = locationDao.insert(locationDistrict)
                } else if (location2s.size == 1) {
                    move2 = true
                    //Location already exists. Getting uid.
                    locationDistrict.locationUid = location2s[0].locationUid
                } else {
                    view.addError("WARNING: More than one instances of " + locationDistrictTitle +
                            " location. Please delete duplicate Locations.",
                            false)
                    move2 = true
                    //Location already exists. Getting uid.
                    locationDistrict.locationUid = location2s[0].locationUid

                }

                if (move2) {
                    val location3s = locationDao.findByTitle(locationTownTitle)
                    var move3 = false
                    if (location3s.size == 0) {
                        move3 = true
                        locationTown.parentLocationUid = locationDistrict.locationUid
                        locationTown.locationUid = locationDao.insert(locationTown)
                    } else if (location3s.size == 1) {
                        move3 = true
                        locationTown.locationUid = location3s[0].locationUid
                    } else {
                        view.addError("WARNING: More than one instances of " + locationTownTitle +
                                " location. Please delete duplicate Locations.",
                                false)
                        move3 = true
                        locationTown.locationUid = location3s[0].locationUid
                    }

                    if (move3) {
                        //Move on the the leaf level
                        val locationLeafs = locationDao.findByTitle(locationLeafTitle)

                        var moveOn = false
                        if (locationLeafs.size == 0) {
                            //Location leaf not created. Setting parent and
                            //persisting to get uid.
                            locationLeaf.parentLocationUid = locationTown.locationUid
                            locationLeaf.locationUid = locationDao.insert(locationLeaf)
                            moveOn = true

                        } else if (locationLeafs.size == 1) {
                            //Location leaf already exists. Getting uid
                            locationLeaf.locationUid = locationLeafs[0].locationUid
                            moveOn = true
                        } else {
                            view.addError("WARNING: More than one instances of " + locationLeafTitle +
                                    " location. Please delete duplicate Locations.",
                                    false)
                            locationLeaf.locationUid = locationLeafs[0].locationUid
                            moveOn = true
                        }

                        //Moving forward
                        if (moveOn) {
                            processClazz(bulkLine, locationLeaf)
                        }
                    }
                }
            }
        }
    }

    private fun processClazz(bulkLine: BulkUploadLine, locationLeaf: Location) {
        //2. Class
        val clazzName = bulkLine.class_name
        val clazzLocation = bulkLine.class_location

        //Get clazz by name:
        val clazzes = clazzDao.findByClazzName(clazzName)
        val thisClazz: Clazz
        if (clazzes.size == 0) {   //No clazzes with that name.
            //Create clazz
            thisClazz = Clazz()
            thisClazz.clazzName = clazzName

            thisClazz.clazzUid = clazzDao.insert(thisClazz)

            //Add location
            val clazzLocations = locationDao.findByTitle(clazzLocation)
            if (clazzLocations.size > 0) {
                if (clazzLocations.size > 1) {
                    //Maybe we alert user that multi locations ?
                }
                //Location exists and is unique
                val thisClazzLocation = clazzLocations[0]
                thisClazz.clazzLocationUid = thisClazzLocation.locationUid
                thisClazz.isClazzActive = true
                thisClazz.clazzLocationUid = locationLeaf.locationUid

                clazzDao.update(thisClazz)

                //Class Custom field
                val classCustomIterator = bulkLine.classCustomFieldToIndex.entries.iterator()
                while (classCustomIterator.hasNext()) {
                    val customFieldAndColMap = classCustomIterator.next()
                    val customFieldUid = customFieldAndColMap.key
                    val colIndex = customFieldAndColMap.value
                    val value = bulkLine.data!![colIndex]
                    if (value != null && !value.isEmpty()) {
                        persistCustomFieldSync(customFieldUid, thisClazz.clazzUid, value)
                    }
                }


                //Move on
                checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_TEACHER)

            } else {
                //Location does not exist.
                view.addError("ERROR: LOCATION : " + clazzLocation
                        + " DOESN'T EXIST", true)
                thereWasAnError = true
            }

        } else if (clazzes.size > 1) {   //Multiple clazzes with that name (ERROR)
            view.addError("ERROR : MULTIPLE CLAZZ WITH NAME: $clazzName", true)
            thereWasAnError = true
        } else {
            thisClazz = clazzes[0]
            //Not updating clazz. Moving on
            checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_TEACHER)
        }

    }

    private fun persistCustomFieldSync(fieldUid: Long, entityUid: Long, value: String?) {
        var customValue: CustomFieldValue? = customFieldValueDao.findValueByCustomFieldUidAndEntityUidSync(
                fieldUid, entityUid)

        if (customValue == null) {
            customValue = CustomFieldValue()
            customValue.customFieldValueFieldUid = fieldUid
            customValue.customFieldValueEntityUid = entityUid
            customValue.customFieldValueValue = value
            customFieldValueDao.insert(customValue)
        } else {
            customValue.customFieldValueValue = value
            customFieldValueDao.update(customValue)
        }
    }

    private fun checkPerson(thisClazz: Clazz, bulkLine: BulkUploadLine, role: Int) {
        val personUid = bulkLine.person_id
        var teacherUsername = bulkLine.teacher_username
        val teacherId = bulkLine.teacher_id
        val teacherFirstName = bulkLine.teacher_first_name
        val teacherLastName = bulkLine.teacher_last_name
        val teacherPhoneNo = bulkLine.teacher_phone_no

        val stuentFirstName = bulkLine.first_name
        val studentLastName = bulkLine.last_name
        val studentGender = bulkLine.gender
        val studentMotherName = bulkLine.mother_name
        val studentMotherNum = bulkLine.mother_num
        val studentPhoneNo = bulkLine.phone_no
        val studentSchool = bulkLine.school
        val studentAddress = bulkLine.address
        val studentFatherName = bulkLine.father_name
        val studentFatherNum = bulkLine.father_num
        val studentDateOfBirth = bulkLine.dob

        val username: String


        if (role == ClazzMember.ROLE_TEACHER) {
            if (teacherUsername.isEmpty() && !teacherId.isEmpty()) {
                teacherUsername = teacherId
            }
            username = teacherUsername
        } else {
            username = personUid
        }

        val thePerson = personDao.findByUsername(username)

        if (thePerson != null) {
            //person object exists - Not creating extra fields (inc custom fields)

            //Still create Entity Role for this different Clazz
            // and //Check for ClazzMember then  directly
            createEntityRoleForTeacherAndClazz(role, thisClazz, thePerson.personUid,
                    bulkLine)

        } else {
            //Create new person
            val person = Person()

            if (role == ClazzMember.ROLE_TEACHER) {
                person.firstNames = teacherFirstName
                person.lastName = teacherLastName
                if (!teacherPhoneNo.isEmpty())
                    person.phoneNum = teacherPhoneNo
                person.username = username

            } else {
                person.username = username
                person.firstNames = stuentFirstName
                person.lastName = studentLastName
                if (!studentMotherName.isEmpty())
                    person.motherName = (studentMotherName)
                if (!studentMotherNum.isEmpty()) {
                    person.motherNum = (studentMotherNum)
                }
                if (!studentFatherName.isEmpty())
                    person.fatherName = (studentFatherName)
                if (!studentFatherNum.isEmpty())
                    person.fatherNumber = (studentFatherNum)
                if (!studentAddress.isEmpty())
                    person.personAddress = (studentAddress)
                person.phoneNum = studentPhoneNo
                person.dateOfBirth = (getDOBFromString(studentDateOfBirth))
                if (studentGender.toLowerCase().startsWith("f")) {
                    person.gender = Person.GENDER_FEMALE
                } else if (studentGender.toLowerCase().startsWith("m")) {
                    person.gender = Person.GENDER_MALE
                }
            }

            person.active = true

            GlobalScope.launch {
                val personWithGroup = personDao.createPersonWithGroupAsync(person)
                val personPersonUid = personWithGroup.personUid
                val personGroupUid = personWithGroup.personGroupUid


                //Creating custom fields (new way)
                if (role == ClazzMember.ROLE_STUDENT) {

                    //Student Custom field
                    val studentCustomIterator = bulkLine.studentCustomFieldToIndex.entries.iterator()
                    while (studentCustomIterator.hasNext()) {
                        val customFieldAndColMap = studentCustomIterator.next()
                        val customFieldUid = customFieldAndColMap.key
                        val colIndex = customFieldAndColMap.value
                        val value = bulkLine.data!![colIndex]
                        if (value != null && !value.isEmpty()) {
                            persistCustomFieldSync(customFieldUid, person.personUid, value)
                        }
                    }

                } else if (role == ClazzMember.ROLE_TEACHER) {
                    //Teacher Custom field
                    val teacherCustomIterator = bulkLine.teacherCustomFieldToIndex.entries.iterator()
                    while (teacherCustomIterator.hasNext()) {
                        val customFieldAndColMap = teacherCustomIterator.next()
                        val customFieldUid = customFieldAndColMap.key
                        val colIndex = customFieldAndColMap.value
                        val value = bulkLine.data!![colIndex]
                        if (value != null && !value.isEmpty()) {
                            persistCustomFieldSync(customFieldUid, person.personUid, value)
                        }
                    }

                }

                if (personPersonUid != null && personGroupUid != null) {
                    //Done teachers person - create clazzMember

                    if (role == ClazzMember.ROLE_TEACHER) {

                        //Create EntityRole
                        val entityRole = EntityRole()
                        entityRole.erTableId = Clazz.TABLE_ID
                        entityRole.erEntityUid = thisClazz.clazzUid
                        entityRole.erRoleUid = teacherRoleUid

                        val entityRoleUid = entityRoleDao.insert(entityRole)

                        if (entityRoleUid != null) {

                            //For a specific clazz
                            val newEntityClazzSpecific = EntityRole()
                            newEntityClazzSpecific.erGroupUid = personGroupUid
                            newEntityClazzSpecific.erRoleUid = teacherRoleUid
                            newEntityClazzSpecific.erTableId = Clazz.TABLE_ID
                            newEntityClazzSpecific.erEntityUid = thisClazz.clazzUid

                            val entityRoleDaoUid = entityRoleDao.insert(newEntityClazzSpecific)

                            if (entityRoleDaoUid != null) {
                                checkClazzMember(thisClazz, bulkLine,
                                        personPersonUid!!, role)
                            } else {
                                view.addError("Something went wrong in clazz entity roles", true)
                                thereWasAnError = true
                            }

                        } else {
                            view.addError("Unable to persist EntityRole ", true)
                            thereWasAnError = true
                        }

                    } else {
                        //Create ClazzMember
                        checkClazzMember(thisClazz, bulkLine,
                                personPersonUid!!, role)
                    }

                } else {
                    view.addError("ERROR: UNABLE TO PERSIST PERSON!", true)
                    thereWasAnError = true
                }
            }
        }

    }

    private fun checkClazzMember(thisClazz: Clazz, bulkLine: BulkUploadLine, personPersonUid: Long,
                                 role: Int) {

        val clazzMember = clazzMemberDao.findByPersonUidAndClazzUid(personPersonUid,
                thisClazz.clazzUid)

        if (clazzMember == null) {
            //Create one.
            val personClazzMember: ClazzMember
            personClazzMember = ClazzMember()
            personClazzMember.clazzMemberPersonUid = personPersonUid
            personClazzMember.clazzMemberClazzUid = thisClazz.clazzUid
            personClazzMember.clazzMemberActive = true
            personClazzMember.clazzMemberDateJoined = UMCalendarUtil.getDateInMilliPlusDays(0)
            personClazzMember.clazzMemberRole = role

            val clazzMemberUid = clazzMemberDao.insert(personClazzMember)

            if (clazzMemberUid != null) {
                if (role == ClazzMember.ROLE_TEACHER) {
                    checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_STUDENT)
                } else {
                    processNextLine()

                }
            } else {
                view.addError("ERROR: UNABLE TO PERSIST CLAZZMEMBER??", true)
                thereWasAnError = true
            }


        } else {
            //Exists already
            if (role == ClazzMember.ROLE_TEACHER) {
                checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_STUDENT)
            } else {
                processNextLine()

            }
        }
    }

    private fun createEntityRoleForTeacherAndClazz(role: Int, thisClazz: Clazz, personPersonUid: Long,
                                                   bulkLine: BulkUploadLine) {
        if (role == ClazzMember.ROLE_TEACHER) {

            val allGroups = personGroupMemberDao.findAllGroupWherePersonIsInSync(personPersonUid)

            val personGroupUid: Long
            if (allGroups != null && allGroups.size > 0) {
                //Get parent group :  ASSUMING ITS THE FIRST ONE
                personGroupUid = allGroups[0].groupMemberGroupUid

                //Create EntityRole
                val entityRole = EntityRole()
                entityRole.erTableId = Clazz.TABLE_ID
                entityRole.erEntityUid = thisClazz.clazzUid
                entityRole.erRoleUid = teacherRoleUid

                val entityRoles = entityRoleDao.findByEntitiyAndPersonGroupSync(
                        Clazz.TABLE_ID, thisClazz.clazzUid, personGroupUid)


                if (entityRoles.isEmpty()) {
                    //Good, create one.

                    val entityRoleUid = entityRoleDao.insert(entityRole)

                    if (entityRoleUid != null) {

                        //For a specific clazz
                        val newEntityClazzSpecific = EntityRole()
                        newEntityClazzSpecific.erGroupUid = personGroupUid
                        newEntityClazzSpecific.erRoleUid = teacherRoleUid
                        newEntityClazzSpecific.erTableId = Clazz.TABLE_ID
                        newEntityClazzSpecific.erEntityUid = thisClazz.clazzUid
                        val entityRoleDaoUid = entityRoleDao.insert(newEntityClazzSpecific)

                        if (entityRoleDaoUid != null) {
                            checkClazzMember(thisClazz, bulkLine,
                                    personPersonUid, role)
                        } else {
                            view.addError("Something went wrong in clazz entity roles", true)
                            thereWasAnError = true
                        }
                    } else {
                        view.addError("Unable to persist entity role", true)
                        thereWasAnError = true
                    }
                } else {
                    //Already created. continue.
                    checkClazzMember(thisClazz, bulkLine, personPersonUid, role)
                }
            }
        } else {
            checkClazzMember(thisClazz, bulkLine, personPersonUid, role)
        }
    }

    private fun getRole() {

        GlobalScope.launch {
            val teacherRole = roleDao.findByName(Role.ROLE_NAME_TEACHER)
            if (teacherRole != null) {
                teacherRoleUid = teacherRole.roleUid
            } else {
                view.showMessage("Please wait until the app syncs and try again.")
            }
        }
    }

    private fun getDOBFromString(dateString: String): Long {

        //TODO: KMP Make it aware of Locale
        var dateLong =  UMCalendarUtil.getLongDateFromStringAndFormat(
                dateString, "dd/MM/yyyy", null)
        if(dateLong==null){
            return 0
        }else{
            return dateLong
        }
    }

    fun setCurrentPosition(currentPosition: Int) {
        this.currentPosition = currentPosition
    }

    /**
     * Bulk upload line class
     */
    inner class BulkUploadLine internal constructor() {

        private var line: String? = null
        var data: Array<String>? = null
            private set
        private var header: Array<String>? = null
        private var headerLine: String? = null
        private val customClassData: Array<String>? = null
        private val customStudentData: Array<String>? = null
        private val customTeacherData: Array<String>? = null

        var studentCustomFieldToIndex: HashMap<Long, Int>
        var teacherCustomFieldToIndex: HashMap<Long, Int>
        var classCustomFieldToIndex: HashMap<Long, Int>

        internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
        internal var customFieldDao = repository.customFieldDao

        internal var person_id = ""
        internal var first_name = ""
        internal var father_name = ""
        internal var family_name = ""
        internal var username = ""
        internal var password = ""
        internal var last_name = ""
        internal var gender = ""
        internal var dob = ""
        internal var mother_name = ""
        internal var phone_no = ""
        internal var address = ""
        internal var school = ""
        internal var location1 = ""
        internal var location2 = ""
        internal var location3 = ""
        internal var locationLeaf = ""
        internal var class_name = ""
        internal var class_location = ""
        internal var teacher_name = ""
        internal var teacher_phone_no = ""
        internal var teacher_first_name = ""
        internal var teacher_last_name = ""
        internal var teacher_id = ""
        internal var teacher_username = ""
        internal var teacher_password = ""
        internal var mother_num = ""
        internal var father_num = ""
        internal var location_id: String? = null

        private val CSV_DELIMITTER = ","

        private var INDEX_PERSON_ID = -1
        private var INDEX_FIRST_NAME = -1
        private var INDEX_FATHER_NAME = -1
        private val INDEX_FAMILY_NAME = -1
        private val INDEX_USERNAME = -1
        private val INDEX_PASSWORD = -1
        private var INDEX_LAST_NAME = -1
        private var INDEX_GENDER = -1
        private var INDEX_DOB = -1
        private var INDEX_MOTHER_NAME = -1
        private var INDEX_MOTHER_NUMBER = -1
        private var INDEX_FATHER_NUMBER = -1
        private val INDEX_PHONE_NO = -1
        private var INDEX_ADDRESS = -1
        private val INDEX_CURRENT_SCHOOL = -1
        private var INDEX_LOCATION_NAME1 = -1
        private var INDEX_LOCATION_NAME2 = -1
        private var INDEX_LOCATION_NAME3 = -1
        private var INDEX_LOCATION_LEAF_NAME = -1
        private var INDEX_CLASS_NAME = -1
        private var INDEX_CLASS_LOCATION = -1
        private val INDEX_TEACHER_NAME = -1
        private var INDEX_TEACHER_PHONE_NO = -1
        private var INDEX_TEACHER_FIRST_NAME = -1
        private var INDEX_TEACHER_LAST_NAME = -1
        private var INDEX_TEACHER_ID = -1
        private var INDEX_TEACHER_USERNAME = -1
        private val INDEX_TEACHER_PASSWORD = -1
        private var INDEX_LOCATION_ID = -1

        init {
            studentCustomFieldToIndex = HashMap()
            teacherCustomFieldToIndex = HashMap()
            classCustomFieldToIndex = HashMap()
        }

        internal fun reset() {
            person_id = ""
            first_name = ""
            father_name = ""
            family_name = ""
            username = ""
            password = ""
            last_name = ""
            gender = ""
            dob = ""
            mother_name = ""
            phone_no = ""
            address = ""
            school = ""
            location1 = ""
            location2 = ""
            location3 = ""
            locationLeaf = ""
            class_name = ""
            class_location = ""
            teacher_name = ""
            teacher_phone_no = ""
            teacher_first_name = ""
            teacher_last_name = ""
            teacher_id = ""
            teacher_username = ""
            teacher_password = ""
            mother_num = ""
            father_num = ""
            location_id = ""

        }

        internal fun setLine(line: String) {
            this.line = line
            reset()
            this.data = line.split(CSV_DELIMITTER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            //There might be a better way, but i cbb
            if (INDEX_PERSON_ID > -1)
                person_id = data!![INDEX_PERSON_ID]
            if (INDEX_FIRST_NAME > -1)
                first_name = data!![INDEX_FIRST_NAME]
            if (INDEX_FATHER_NAME > -1)
                father_name = data!![INDEX_FATHER_NAME]
            if (INDEX_FAMILY_NAME > -1)
                family_name = data!![INDEX_FAMILY_NAME]
            if (INDEX_USERNAME > -1)
                username = data!![INDEX_USERNAME]
            if (INDEX_PASSWORD > -1)
                password = data!![INDEX_PASSWORD]
            if (INDEX_LAST_NAME > -1)
                last_name = data!![INDEX_LAST_NAME]
            if (INDEX_GENDER > -1)
                gender = data!![INDEX_GENDER]
            if (INDEX_DOB > -1)
                dob = data!![INDEX_DOB]
            if (INDEX_MOTHER_NAME > -1)
                mother_name = data!![INDEX_MOTHER_NAME]
            if (INDEX_PHONE_NO > -1)
                phone_no = data!![INDEX_PHONE_NO]
            if (INDEX_ADDRESS > -1)
                address = data!![INDEX_ADDRESS]
            if (INDEX_CURRENT_SCHOOL > -1)
                school = data!![INDEX_CURRENT_SCHOOL]
            if (INDEX_LOCATION_NAME1 > -1)
                location1 = data!![INDEX_LOCATION_NAME1]
            if (INDEX_LOCATION_NAME2 > -1)
                location2 = data!![INDEX_LOCATION_NAME2]
            if (INDEX_LOCATION_NAME3 > -1)
                location3 = data!![INDEX_LOCATION_NAME3]
            if (INDEX_LOCATION_LEAF_NAME > -1)
                locationLeaf = data!![INDEX_LOCATION_LEAF_NAME]
            if (INDEX_CLASS_NAME > -1)
                class_name = data!![INDEX_CLASS_NAME]
            if (INDEX_CLASS_LOCATION > -1)
                class_location = data!![INDEX_CLASS_LOCATION]
            if (INDEX_TEACHER_NAME > -1)
                teacher_name = data!![INDEX_TEACHER_NAME]
            if (INDEX_TEACHER_PHONE_NO > -1)
                teacher_phone_no = data!![INDEX_TEACHER_PHONE_NO]
            if (INDEX_TEACHER_FIRST_NAME > -1)
                teacher_first_name = data!![INDEX_TEACHER_FIRST_NAME]
            if (INDEX_TEACHER_LAST_NAME > -1)
                teacher_last_name = data!![INDEX_TEACHER_LAST_NAME]
            if (INDEX_TEACHER_ID > -1)
                teacher_id = data!![INDEX_TEACHER_ID]
            if (INDEX_TEACHER_USERNAME > -1)
                teacher_username = data!![INDEX_TEACHER_USERNAME]
            if (INDEX_TEACHER_PASSWORD > -1)
                teacher_password = data!![INDEX_TEACHER_PASSWORD]
            if (INDEX_MOTHER_NUMBER > -1)
                mother_num = data!![INDEX_MOTHER_NUMBER]
            if (INDEX_FATHER_NUMBER > -1)
                father_num = data!![INDEX_FATHER_NUMBER]
            if (INDEX_LOCATION_ID > -1)
                location_id = data!![INDEX_LOCATION_ID]

        }

        internal fun getCamelCaseFromTypeCase(typeCase: String): String {
            var typeCase = typeCase

            var titleCase = ""
            typeCase = typeCase.toLowerCase()
            val allWords = typeCase.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var first = true
            for (i in allWords.indices) {
                val word = allWords[i]
                if (!word.isEmpty()) {
                    var camelWord = word
                    if (first) {
                        first = false
                    } else {
                        camelWord = word.substring(0, 1).toUpperCase() + word.substring(1)
                    }

                    titleCase = titleCase + camelWord
                }
            }
            return titleCase
        }

        internal fun processHeader(headerLine: String) {
            this.headerLine = headerLine
            var colIndex = 0
            this.header = headerLine.split(CSV_DELIMITTER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (i in header!!.indices) {

                var everyHeader = header!![i]
                everyHeader = everyHeader.trim { it <= ' ' }
                everyHeader = everyHeader.replace("\\P{Print}".toRegex(), "")
                everyHeader = everyHeader.replace("\uFEFF", "")
                everyHeader = everyHeader.toLowerCase()
                if (everyHeader.startsWith("student ")) {
                    val fieldTC = getCamelCaseFromTypeCase(everyHeader.substring("student ".length))
                    when (fieldTC) {
                        "personId" -> INDEX_PERSON_ID = colIndex
                        "firstNames" -> INDEX_FIRST_NAME = colIndex
                        "lastName" -> INDEX_LAST_NAME = colIndex
                        "gender" -> INDEX_GENDER = colIndex
                        "dob" -> INDEX_DOB = colIndex
                        "motherName" -> INDEX_MOTHER_NAME = colIndex
                        "motherNum" -> INDEX_MOTHER_NUMBER = colIndex
                        "fatherName" -> INDEX_FATHER_NAME = colIndex
                        "fatherNumber" -> INDEX_FATHER_NUMBER = colIndex
                        "personAddress" -> INDEX_ADDRESS = colIndex
                        else ->
                            //Lookup custom field
                            findCustomField(everyHeader.substring("student ".length), Person.TABLE_ID, colIndex, CUSTOM_FIELD_STUDENT)
                    }

                } else if (everyHeader.startsWith("teacher ")) {
                    val fieldTC = getCamelCaseFromTypeCase(everyHeader.substring("teacher ".length))
                    when (fieldTC) {
                        "personId" -> INDEX_TEACHER_ID = colIndex
                        "firstNames" -> INDEX_TEACHER_FIRST_NAME = colIndex
                        "lastName" -> INDEX_TEACHER_LAST_NAME = colIndex
                        "phoneNum" -> INDEX_TEACHER_PHONE_NO = colIndex
                        "username" -> INDEX_TEACHER_USERNAME = colIndex
                        else -> findCustomField(everyHeader.substring("teacher ".length), Person.TABLE_ID, colIndex, CUSTOM_FIELD_TEACHER)
                    }
                } else if (everyHeader.startsWith("class ")) {
                    val fieldTC = getCamelCaseFromTypeCase(everyHeader.substring("class ".length))
                    when (fieldTC) {
                        "name" -> INDEX_CLASS_NAME = colIndex
                        "location" -> INDEX_CLASS_LOCATION = colIndex
                        else ->
                            //Find Custom
                            findCustomField(everyHeader.substring("class ".length), Clazz.TABLE_ID, colIndex, CUSTOM_FIELD_CLASS)
                    }

                } else if (everyHeader.startsWith("location ")) {
                    val fieldTC = getCamelCaseFromTypeCase(everyHeader.substring("location ".length))
                    when (fieldTC) {
                        "id" -> INDEX_LOCATION_ID = colIndex
                        "governorate" -> INDEX_LOCATION_NAME1 = colIndex
                        "district" -> INDEX_LOCATION_NAME2 = colIndex
                        "town" -> INDEX_LOCATION_NAME3 = colIndex
                        "name" -> INDEX_LOCATION_LEAF_NAME = colIndex
                        else -> {
                            //NO CUSTOM FIELD FOR LOCATION.
                            view.addError("Can't find location value: $fieldTC", true)
                            thereWasAnError = true
                        }
                    }

                }//else nothing to process. Error log it ?
                else {
                    view.addError("Error cannot figure what this is: $everyHeader", true)
                    thereWasAnError = true
                }

                colIndex++
            }

        }

        internal fun findCustomField(fieldName: String, entity: Int, colIndex: Int, type: Int) {
            //Convert name to space name

            GlobalScope.launch {
                val result = customFieldDao.findByFieldNameAndEntityTypeAsync(fieldName, entity)
                val cf: CustomField
                if (result != null && result.size > 0) {
                    cf = result[0]
                    when (type) {
                        CUSTOM_FIELD_STUDENT -> studentCustomFieldToIndex[cf.customFieldUid] = colIndex
                        CUSTOM_FIELD_TEACHER -> teacherCustomFieldToIndex[cf.customFieldUid] = colIndex
                        CUSTOM_FIELD_CLASS -> classCustomFieldToIndex[cf.customFieldUid] = colIndex
                    }
                } else {
                    var typeString = ""
                    when (type) {
                        CUSTOM_FIELD_STUDENT -> typeString = "Student"
                        CUSTOM_FIELD_TEACHER -> typeString = "Teacher"
                        CUSTOM_FIELD_CLASS -> typeString = "Class"
                    }
                    view.addError("Cannot process " + typeString + " custom field : " +
                            fieldName, false)
                }
            }
        }

    }

    companion object {

        val CUSTOM_FIELD_STUDENT = 1
        val CUSTOM_FIELD_TEACHER = 2
        val CUSTOM_FIELD_CLASS = 3
    }

}
