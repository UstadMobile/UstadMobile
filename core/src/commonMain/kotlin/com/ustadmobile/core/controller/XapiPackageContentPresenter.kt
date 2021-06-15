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
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.setInputString
import io.ktor.client.*
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
        } ?: return

        contextRegistration = randomUuid().toString()

        GlobalScope.launch {
            mountedPath = mounter.mountContainer(activeEndpoint, containerUid)
            val client: HttpClient = di.direct.instance()
            val tincanContent = client.get<String>(UMFileUtil.joinPaths(mountedPath, "tincan.xml"))

            val xppFactory : XmlPullParserFactory = di.direct.instance(tag = DiTag.XPP_FACTORY_NSAWARE)
            val xpp = xppFactory.newPullParser()
            xpp.setInputString(tincanContent)
            tinCanXml = TinCanXML.loadFromXML(xpp)
            val launchHref = tinCanXml?.launchActivity?.launchUrl
            val actorJsonStr: String = if(learnerGroupUid == 0L){
                Json.encodeToString(UmAccountActor.serializer(),
                        accountManager.activeAccount.toXapiActorJsonObject(context))
            }else{
                val memberList = repo.learnerGroupMemberDao.findLearnerGroupMembersByGroupIdAndEntryList(
                        learnerGroupUid,contentEntryUid)
                Json.encodeToString(UmAccountGroupActor.serializer(),
                        accountManager.activeAccount.toXapiGroupJsonObject(memberList))
            }
            val launchMethodParams = mapOf(
                    "actor" to actorJsonStr,
                    "endpoint" to UMFileUtil.resolveLink(mountedPath,
                            "/${UMURLEncoder.encodeUTF8(activeEndpoint)}/xapi/$contentEntryUid/$clazzUid/"),
                    "auth" to "OjFjMGY4NTYxNzUwOGI4YWY0NjFkNzU5MWUxMzE1ZGQ1",
                    "registration" to contextRegistration,
                    "activity_id" to (tinCanXml?.launchActivity?.id ?: "xapi_id"))
            if(launchHref != null) {
                val launchUrl = UMFileUtil.joinPaths(mountedPath, launchHref) + "?"  +
                        launchMethodParams.toQueryString()
                view.runOnUiThread(Runnable {
                    view.contentTitle = tinCanXml?.launchActivity?.name ?: ""
                    view.url = launchUrl
                })
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if(accountManager.activeAccount.personUid == 0L)
            return //no one is really logged in

        GlobalScope.launch {
            val contentEntry = db.contentEntryDao.findByUid(contentEntryUid) ?: return@launch
            if(contentEntry.completionCriteria != ContentEntry.COMPLETION_CRITERIA_MIN_SCORE) return@launch
            val score = db.statementDao.findScoreForSession(contextRegistration) ?: return@launch
            val scoreTotal = (score.resultScore.toFloat() / score.resultMax) * 100
            if(scoreTotal > contentEntry.minScore){
                statementEndpoint.storeCompletedStatement(accountManager.activeAccount,
                        contentEntry, contextRegistration, score, clazzUid)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GlobalScope.launch (Dispatchers.Main){
            if(mountedPath.isNotEmpty()){
                mounter.unMountContainer(mountedEndpoint, mountedPath)
            }
        }
    }



}
