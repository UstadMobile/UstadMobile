package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.ContentPlugin
import com.ustadmobile.core.contentjob.ProcessResult
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.torrent.UstadTorrentManager
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import org.kodein.di.DI


expect suspend fun ContentPlugin.withWifiLock(context: Any, block: suspend () -> Unit)

expect suspend fun deleteFilesForContentEntry(contentEntryUid: Long, di: DI, endpoint: Endpoint): Int