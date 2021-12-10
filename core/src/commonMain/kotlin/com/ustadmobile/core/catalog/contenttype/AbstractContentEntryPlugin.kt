package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.ContentPlugin
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

/**
 * This is simply used to hold a common extractMetadata function that is used for both contententry
 * branches and leaves.
 */
abstract class AbstractContentEntryPlugin(
    protected var context: Any,
    protected val endpoint: Endpoint,
    override val di: DI,
) : ContentPlugin, DIAware{

    protected val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    protected val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)


    protected suspend fun extractMetadata(
        viewName: String,
        uri: DoorUri
    ): MetadataResult? {
        val uriStr: String = uri.toString()
        if(uriStr.contains(UstadMobileSystemCommon.LINK_ENDPOINT_VIEWNAME_DIVIDER + viewName)) {
            val entityUid = UMFileUtil.parseURLQueryString(uriStr)[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L
            val contentEntry = repo.contentEntryDao.findByUidWithLanguageAsync(entityUid) ?: return null
            return MetadataResult(contentEntry, pluginId)
        }else {
            return null
        }
    }

}