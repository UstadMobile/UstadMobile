package com.ustadmobile.core.controller

import com.ustadmobile.core.account.AdultAccountRequiredException
import com.ustadmobile.core.account.ConsentNotGrantedException
import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.ext.verifySite
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.PersonEditView.Companion.REGISTER_VIA_LINK
import com.ustadmobile.core.view.RegisterAgeRedirectView
import com.ustadmobile.core.view.SiteTermsDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_INTENT_MESSAGE
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_POPUPTO_ON_FINISH
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_SITE
import com.ustadmobile.lib.db.entities.Site
import io.github.aakira.napier.Napier
import io.ktor.client.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class Login2Presenter(context: Any, arguments: Map<String, String>, view: Login2View,
                      di: DI)
    : UstadBaseController<Login2View>(context, arguments, view, di, activeSessionRequired = false) {

    private  lateinit var nextDestination: String


    private lateinit var serverUrl: String

    private val impl: UstadMobileSystemImpl by instance()

    private val accountManager: UstadAccountManager by instance()

    private val httpClient: HttpClient by instance()

    private var workSpace: Site? = null

    private var siteLoadJob: Job? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        nextDestination = arguments[ARG_NEXT] ?: impl.getAppConfigDefaultFirstDest(context)

        serverUrl = if (arguments.containsKey(ARG_SERVER_URL)) {
            arguments.getValue(ARG_SERVER_URL)
        } else {
            impl.getAppConfigString(
                    AppConfig.KEY_API_URL, "http://localhost", context)?:""
        }

        view.versionInfo = impl.getVersion(context)
        serverUrl = serverUrl.requirePostfix("/")
        view.loginIntentMessage = arguments[ARG_INTENT_MESSAGE]
        val mSite = arguments[ARG_SITE]
        if(mSite != null){
            onVerifySite(safeParse(di, Site.serializer(), mSite))
        }else{
            view.loading = true
            view.inProgress = true

           siteLoadJob = presenterScope.launch {
               while(workSpace == null) {
                   try {
                       val site = httpClient.verifySite(serverUrl, 10000)
                       onVerifySite(site) // onVerifySite will set the workspace var, and exit the loop
                   }catch(e: Exception) {
                       Napier.w("Could not load site object for $serverUrl", e)
                       view.errorMessage = impl.getString(MessageID.login_network_error, context)
                       delay(10000)
                   }
               }
           }

        }
    }

    fun onVerifySite(site: Site) {
        workSpace = site
        view.createAccountVisible = site.registrationAllowed
        view.connectAsGuestVisible = site.guestLogin

        view.loading = false
        view.inProgress = false

        if(view.errorMessage == impl.getString(MessageID.login_network_error, context))
            view.errorMessage = ""
    }

    /**
     * After the user has logged in successfully or selected to proceed as a guest, go to the next
     * destination as per the arguments. This includes popping off the stack (using ARG_POPUPTO_ON_FINISH
     * or at least removing the login screen itself from the stack).
     */
    private fun goToNextDestAfterLoginOrGuestSelected() {
        val goOptions = UstadMobileSystemCommon.UstadGoOptions(
                arguments[ARG_POPUPTO_ON_FINISH] ?: UstadView.ROOT_DEST,
                false)
        Napier.d { "LoginPresenter: go to next destination: $nextDestination" }

        //This seems wrong: if nextdestination has arguments, what then?
        impl.go(nextDestination, mapOf(), context, goOptions)
    }

    fun handleLogin(username: String?, password:String?){
        view.inProgress = true
        view.loading = true
        view.isEmptyUsername = username == null || username.isEmpty()
        view.isEmptyPassword = password == null || password.isEmpty()

        if(username != null && username.isNotEmpty() && password != null && password.isNotEmpty()){
            presenterScope.launch {
                try {
                    accountManager.login(username.trim(), password.trim(), serverUrl,
                        arguments[UstadView.ARG_MAX_DATE_OF_BIRTH]?.toLong() ?: 0L)
                    goToNextDestAfterLoginOrGuestSelected()
                }catch(e: AdultAccountRequiredException) {
                    view.errorMessage = impl.getString(MessageID.adult_account_required, context)
                } catch(e: UnauthorizedException) {
                    view.errorMessage = impl.getString(MessageID.wrong_user_pass_combo,
                        context)
                } catch(e: ConsentNotGrantedException) {
                    view.errorMessage = impl.getString(MessageID.your_account_needs_approved,
                        context)
                }catch(e: Exception) {
                    Napier.e("Error logging in", e)
                    view.errorMessage = impl.getString(MessageID.login_network_error, context)
                }finally {
                    view.inProgress = false
                    view.loading = false
                    view.clearFields()
                }
            }
        }else{
            view.inProgress = false
            view.loading = false
        }
    }

    fun handleCreateAccount(){
        val args = mutableMapOf(
                ARG_SERVER_URL to serverUrl,
                SiteTermsDetailView.ARG_SHOW_ACCEPT_BUTTON to true.toString(),
                SiteTermsDetailView.ARG_USE_DISPLAY_LOCALE to true.toString(),
                ARG_POPUPTO_ON_FINISH to (arguments[ARG_POPUPTO_ON_FINISH] ?: Login2View.VIEW_NAME))

        args.putFromOtherMapIfPresent(arguments, ARG_NEXT)
        args.putFromOtherMapIfPresent(arguments, REGISTER_VIA_LINK)

        impl.go(RegisterAgeRedirectView.VIEW_NAME, args, context)
    }

    fun handleConnectAsGuest(){
        presenterScope.launch {
            accountManager.startGuestSession(serverUrl)
            goToNextDestAfterLoginOrGuestSelected()
        }
    }

    override fun onDestroy() {
        siteLoadJob?.cancel()
        siteLoadJob = null

        super.onDestroy()
    }

}
