package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.PersonWithSaleInfoListView.Companion.ARG_LE_UID
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Presenter for UserProfile view
 */
class UserProfilePresenter(context: Any,
                           arguments: Map<String, String?>,
                           view: UserProfileView, val impl: UstadMobileSystemImpl)
    : UstadBaseController<UserProfileView>(context, arguments, view) {

    internal var repository: UmAppDatabase =
            UmAccountManager.getRepositoryForActiveAccount(context)
    private val personDao: PersonDao
    private var loggedInPerson: Person? = null
    private var personPictureDao: PersonPictureDao? = null

    var loggedInPersonUid = 0L

    private val languageOptions = impl.getAllUiLanguage(context)

    init {

        //Get provider Dao
        personDao = repository.personDao
        personPictureDao = repository.personPictureDao

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val activeAccount = UmAccountManager.getActiveAccount(context)

        if (activeAccount != null) {
            loggedInPersonUid = activeAccount.personUid
            GlobalScope.launch {
                val result = personDao.findByUidAsync(loggedInPersonUid)
                loggedInPerson = result
                if (loggedInPerson != null) {
                    val personName = result!!.firstNames + " " + result.lastName
                    view.runOnUiThread(Runnable {
                        view.updateToolbarTitle(personName)
                    })

                    personPictureDao = repository.personPictureDao
                    val personPicture =
                            personPictureDao!!.findByPersonUidAsync(loggedInPerson!!.personUid)
                    if (personPicture != null) {
                        //TODO: Fix for KMP
//                                view.updateImageOnView(personPictureDao!!.getAttachmentPath
//                                (personPicture.personPictureUid))
                    }
                }
            }
        }

        //TODO: Get last time the device/account was synced.
        val lastSyncedText = impl.getString(MessageID.account_last_synced,
                context)
        val lastSyncedText2 = ""
        val lastSynced = lastSyncedText + " " + lastSyncedText2
        view.runOnUiThread(Runnable {
            view.updateLastSyncedText(lastSynced)
        })
    }

    fun handleClickChangePassword() {
        val args = HashMap<String, String>()
        impl.go(ChangePasswordView.VIEW_NAME, args, context)
    }

    fun handleClickChangeLanguage() {
        val args = HashMap<String, String>()
        impl.go(SelectLanguageDialogView.VIEW_NAME, args, context)

    }

    fun handleClickLogout() {
        //TODO: KMP Check this
        val emptyAcccount = UmAccount(0, null, null, null)
        UmAccountManager.setActiveAccount(emptyAcccount, context)
        UmAccountManager.updatePasswordHash(null, context, impl)
        impl.setAppPref(UmAccountManager.PREFKEY_PASSWORD_HASH_USERNAME, "", context)
        val args = HashMap<String, String>()
        impl.go(LoginView.VIEW_NAME, args, context)
    }


    fun handleClickMyWomenEntrepreneurs(){
        val args = HashMap<String, String>()
        args[ARG_LE_UID] = loggedInPersonUid.toString()
        impl.go(PersonWithSaleInfoListView.VIEW_NAME, args, context)
    }

    fun openPictureDialog(imagePath: String) {
        //Open Dialog
        val args = HashMap<String, String>()
        //TODO If needed:
        //        args.put(ARG_PERSON_IMAGE_PATH, imagePath);
        //        args.put(ARG_PERSON_UID, personUid);
        //        impl.go(PersonPictureDialogView.VIEW_NAME, args, context);
    }

    //TODO: Changed to File path instead of File object
    fun handleCompressedImage(imageFilePath: String) {
        val personPictureDao = repository.personPictureDao
        val personPicture = PersonPicture()
        personPicture.personPicturePersonUid = loggedInPersonUid
        personPicture.picTimestamp = DateTime.nowUnixLong() //Check this TODO

        val personDao = repository.personDao

        GlobalScope.launch {
            try {
                val personPictureUid = personPictureDao.insertAsync(personPicture)
                //TODO: fix for KMP
                //personPictureDao.setAttachmentFromTmpFile(personPictureUid, imageFile)

                //Update person and generate feeds for person
                personDao.updateAsync(loggedInPerson!!) //TODO: Check this

                //TODO: Fix for KMP
                //view.updateImageOnView(personPictureDao.getAttachmentPath(personPictureUid))
            }catch(e:Exception){
                println(e.message)
            }


        }
    }

    fun handleUserLogout(){
        UmAccountManager.setActiveAccount(UmAccount(0,
                null, null, null), context)
        val args = HashMap<String, String>()

        val firstDest = impl.getAppConfigString(
                AppConfig.KEY_FIRST_DEST, "BasePoint", context)
        impl.go(firstDest!!, args, context)
    }

    fun handleShowLanguageOptions(){
        view.setLanguageOption(languageOptions.values.sorted().toMutableList())
    }
}
