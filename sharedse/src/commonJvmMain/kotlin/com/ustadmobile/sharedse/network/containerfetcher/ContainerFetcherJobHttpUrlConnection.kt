package com.ustadmobile.sharedse.network.containerfetcher

import com.ustadmobile.core.db.JobStatus
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Callable
import com.ustadmobile.sharedse.network.NetworkManagerWithConnectionOpener
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.content
import com.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import kotlin.coroutines.coroutineContext


typealias ConnectionOpener = (url: URL) -> HttpURLConnection

