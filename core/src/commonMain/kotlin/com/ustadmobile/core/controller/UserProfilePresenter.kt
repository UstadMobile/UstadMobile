package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.PersonPictureDialogView.Companion.ARG_PERSON_IMAGE_PATH
import com.ustadmobile.core.view.PersonPictureDialogView.Companion.ARG_PERSON_UID
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class UserProfilePresenter (context: Any, arguments: Map<String, String?>, view: UserProfileView,
                            val personDao: PersonDao, val impl: UstadMobileSystemImpl)
    : UstadBaseController<UserProfileView>(context, arguments, view){

    internal var repository: UmAppDatabase =
            UmAccountManager.getRepositoryForActiveAccount(context)
    private val languageOptions = impl.getAllUiLanguage(context)
    private var personPictureDao: PersonPictureDao
    private var loggedInPerson: Person? = null

    var loggedInPersonUid = 0L

    init {

        //Get provider Dao
        personPictureDao =
                UmAccountManager.getRepositoryForActiveAccount(context).personPictureDao

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        val account = UmAccountManager.getActiveAccount(context)
        view.setUsername(account!!.username!!)
        view.setCurrentLanguage(languageOptions[impl.getDisplayedLocale(context)!!])

        if (account != null) {
            loggedInPersonUid = account.personUid
            GlobalScope.launch {
                val result = personDao.findByUidAsync(loggedInPersonUid)
                loggedInPerson = result

                if (loggedInPerson != null) {

                    var firstNames = ""
                    var lastName = ""
                    if(result!!.firstNames!=null){
                        firstNames = result!!.firstNames!!
                    }
                    if(result!!.lastName != null){
                        lastName = result!!.lastName!!
                    }

                    val personName = firstNames + " " + lastName
                    view.runOnUiThread(Runnable {
                        view.updateToolbarTitle(personName)
                    })

                    val personPicture =
                            personPictureDao.findByPersonUidAsync(loggedInPerson!!.personUid)
                    if (personPicture != null) {
                        val picturePath = personPictureDao.getAttachmentPath(personPicture)
                        view.updateImageOnView(picturePath!!, true)
                    }
                }
            }
        }
    }

    fun handleClickChangePassword() {
        val args = HashMap<String, String>()
        impl.go(ChangePasswordView.VIEW_NAME, args, context)
    }


    fun handleUserLogout(){
        UmAccountManager.setActiveAccount(UmAccount(0,
                "", "", ""), context)
        UmAccountManager.setActiveAccount(null, context)

        val args = HashMap<String, String>()
        view.runOnUiThread(Runnable {
            view.callFinishAffinity()
        })

        impl.go(LoginView.VIEW_NAME, args, context)
    }

    fun handleShowLanguageOptions(){
        view.setLanguageOption(languageOptions.values.sorted().toMutableList())
    }

    fun handleLanguageSelected(position: Int){
        val languageName = languageOptions.values.sorted().toMutableList()[position]
        val localeCode = getLocaleCode(languageName)
        if(impl.getDisplayedLocale(context) != localeCode){
            impl.setLocale(localeCode, context)
            view.restartUI()
        }
    }

    private fun getLocaleCode(name: String): String{
        for(pair in languageOptions){
            if(name == pair.value) return pair.key
        }
        return "en"
    }

    fun openPictureDialog(imagePath: String) {
        //Open Dialog
        val args = HashMap<String, String>()
        args.put(ARG_PERSON_IMAGE_PATH, imagePath);
        args.put(ARG_PERSON_UID, loggedInPersonUid.toString());
        impl.go(PersonPictureDialogView.VIEW_NAME, args, context);
    }

    fun handleCompressedImage(imageFilePath: String) {

        GlobalScope.launch {
            try {

                var existingPP = personPictureDao.findByPersonUidAsync(loggedInPersonUid)
                if(existingPP == null){
                    existingPP = PersonPicture()
                    existingPP.personPicturePersonUid = loggedInPersonUid
                    existingPP.picTimestamp = UMCalendarUtil.getDateInMilliPlusDays(0)
                    val personPictureUid = personPictureDao.insertAsync(existingPP)
                    existingPP.personPictureUid = personPictureUid
                }

                personPictureDao.setAttachment(existingPP, imageFilePath)
                existingPP.picTimestamp = UMCalendarUtil.getDateInMilliPlusDays(0)
                personPictureDao.update(existingPP)

                //Update person and generate feeds for person
                personDao.updatePersonAsync(loggedInPerson!!, loggedInPersonUid)

                //Update view with path
                val picturePath = personPictureDao.getAttachmentPath(existingPP)
                view.updateImageOnView(picturePath!!, true)
            }catch(e:Exception){
                throw e
            }
        }
    }

}