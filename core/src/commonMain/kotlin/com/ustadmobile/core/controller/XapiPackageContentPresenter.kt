package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.storeCompletedStatement
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.tincan.UmAccountActor
import com.ustadmobile.core.tincan.UmAccountGroupActor
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.util.ext.toXapiActorJsonObject
import com.ustadmobile.core.util.ext.toXapiGroupJsonObject
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.setInputString
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

/**
 * Created by mike on 9/13/17.
 *
 * Displays an XAPI Zip Package.
 *
 * Pass EpubContentPresenter.ARG_CONTAINERURI when creating to provide the location of the xAPI
 * zip to open
 *
 * Uses the Rustici launch method to find the URL to launch:
 * https://github.com/RusticiSoftware/launch/blob/master/lms_lrs.md
 *
 */
class XapiPackageContentPresenter(context: Any, args: Map<String, String>, view: XapiPackageContentView,
                                  di: DI)
    : UstadBaseController<XapiPackageContentView>(context, args, view, di) {

    private var tinCanXml: TinCanXML? = null

    private var mountedPath: String = ""

    private val mounter: ContainerMounter by instance()

    private val accountManager: UstadAccountManager by instance()

    private lateinit var mountedEndpoint: String

    private lateinit var contextRegistration: String

    private var contentEntryUid: Long = 0L

    private var clazzUid: Long = 0L

    private var onCreateException: Exception? = null

    private var isStarted: Boolean = false

    val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_REPO)

    private val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_DB)

    val statementEndpoint by on(accountManager.activeAccount).instance<XapiStatementEndpoint>()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val containerUid = arguments[UstadView.ARG_CONTAINER_UID]?.toLongOrNull() ?: 0L
        contentEntryUid = arguments[UstadView.ARG_CONTENT_ENTRY_UID]?.toLongOrNull() ?: 0L
        val learnerGroupUid = arguments[UstadView.ARG_LEARNER_GROUP_UID]?.toLongOrNull() ?: 0L
        clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLongOrNull() ?: 0L
        val activeEndpoint = accountManager.activeAccount.endpointUrl.also {
            mountedEndpoint = it
        }

        contextRegistration = randomUuid().toString()

        presenterScope.launch {
            try {
                mountedPath = mounter.mountContainer(activeEndpoint, containerUid)
                val client: HttpClient = di.direct.instance()
                val tinCanPath = UMFileUtil.joinPaths(mountedPath, "tincan.xml")
                Napier.d { "XapiPackageContentPresenter: Loading $tinCanPath " }
                val tincanContent = client.get<String>(tinCanPath)

                val xppFactory: XmlPullParserFactory = di.direct.instance(tag = DiTag.XPP_FACTORY_NSAWARE)
                val xpp = xppFactory.newPullParser()
                xpp.setInputString(tincanContent)
                tinCanXml = TinCanXML.loadFromXML(xpp)
                val launchHref = tinCanXml?.launchActivity?.launchUrl
                Napier.d { "XapiPackageContentPresenter: Launch HREF = $launchHref" }
                val actorJsonStr: String = if (learnerGroupUid == 0L) {
                    Json.encodeToString(UmAccountActor.serializer(),
                            accountManager.activeAccount.toXapiActorJsonObject(context))
                } else {
                    val memberList = repo.learnerGroupMemberDao.findLearnerGroupMembersByGroupIdAndEntryList(
                            learnerGroupUid, contentEntryUid)
                    Json.encodeToString(UmAccountGroupActor.serializer(),
                            accountManager.activeAccount.toXapiGroupJsonObject(memberList))
                }

                val endpointPart = "xapi/$contentEntryUid/$clazzUid/"
                val xapiEndPoint = if(UmPlatformUtil.isWeb){
                    UMFileUtil.resolveLink(UMURLEncoder.encodeUTF8(activeEndpoint),endpointPart)
                }else {
                    UMFileUtil.resolveLink(mountedPath,
                        "/${UMURLEncoder.encodeUTF8(activeEndpoint)}/$endpointPart")
                }

                val launchMethodParams = mapOf(
                        "actor" to actorJsonStr,
                        "endpoint" to xapiEndPoint,
                        "auth" to "OjFjMGY4NTYxNzUwOGI4YWY0NjFkNzU5MWUxMzE1ZGQ1",
                        "registration" to contextRegistration,
                        "activity_id" to (tinCanXml?.launchActivity?.id ?: "xapi_id"))
                if (launchHref != null) {
                    val launchUrl = UMFileUtil.joinPaths(mountedPath, launchHref) + "?" +
                            launchMethodParams.toQueryString()
                    Napier.d { "XapiPackageContentPresenter: opening launch url = $launchUrl" }
                    view.contentTitle = tinCanXml?.launchActivity?.name ?: ""
                    view.url = launchUrl
                }else {
                    Napier.e { "XapiPackageContentPresenter: ERR: launchHref = null" }
                }
            }catch (e: Exception){
                if(e !is CancellationException) {
                    if(isStarted){
                        navigateToErrorScreen(e)
                    }else{
                        onCreateException = e
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        isStarted = true
        onCreateException?.also {
            navigateToErrorScreen(it)
        }
        onCreateException = null
    }

    override fun onStop() {
        super.onStop()
        if(accountManager.activeAccount.personUid == 0L)
            return //no one is really logged in

        presenterScope.launch {
            val contentEntry = db.contentEntryDao.findByUidAsync(contentEntryUid) ?: return@launch
            if(contentEntry.completionCriteria != ContentEntry.COMPLETION_CRITERIA_MIN_SCORE) return@launch
            val completedScore = db.statementDao.findCompletedScoreForSession(contextRegistration)
            var scoreTotal = completedScore?.resultScore ?: 0
            val scoreForSession = if((completedScore?.resultScaled ?: 0f) == 0f) {
                // no completed statement found, calculate the score from the session and set the new scoreTotal
               val score = db.statementDao.calculateScoreForSession(contextRegistration) ?: return@launch
                scoreTotal = score.resultScore
                score.resultScaled = (score.resultScore.toFloat() / score.resultMax)
                score
            }else{
                completedScore
            }
            if(scoreTotal >= contentEntry.minScore){
                statementEndpoint.storeCompletedStatement(accountManager.activeAccount,
                        contentEntry, contextRegistration, scoreForSession, clazzUid)
            }
        }
    }

    override fun onDestroy() {
        presenterScope.launch (doorMainDispatcher()){
            if(mountedPath.isNotEmpty()){
                mounter.unMountContainer(mountedEndpoint, mountedPath)
            }
        }

        super.onDestroy()
    }



}
