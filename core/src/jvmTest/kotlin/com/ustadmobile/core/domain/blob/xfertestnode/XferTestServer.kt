package com.ustadmobile.core.domain.blob.xfertestnode

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.core.domain.blob.upload.BlobUploadServerUseCase
import com.ustadmobile.core.domain.cachelock.CreateCacheLocksForActiveContentEntryVersionUseCase
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.http.DoorHttpServerConfig
import com.ustadmobile.door.log.NapierDoorLogger
import com.ustadmobile.ihttp.ext.clientProtocolAndHost
import com.ustadmobile.ihttp.headers.asIHttpHeaders
import com.ustadmobile.lib.rest.CacheRoute
import com.ustadmobile.lib.rest.api.blob.BlobUploadServerRoute
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.pipeline.PipelineContext
import kotlinx.io.files.Path
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import org.kodein.di.on
import org.kodein.di.registerContextTranslator
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.io.Closeable
import java.io.File


typealias XferTestServerInteceptor = suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit

/**
 * Contains the dependencies required to act as a server for blob transfer testing, including a
 * KTOR server and the required Route(s) to receive blob uploads.
 *
 * @param ktorInterceptor an optional interceptor. This can be used to fail requests for testing
 *        purposes.
 */
class XferTestServer(
    val node: XferTestNode,
    val port: Int = 8094,
    val ktorInterceptor: XferTestServerInteceptor? = null,
) {

    private val ktorServer: ApplicationEngine

    val di: DI

    private val diToClose = mutableListOf<Closeable>()

    init {
        di = DI {
            extend(node.di)

            bind<BlobUploadServerUseCase>() with scoped(node.endpointScope).singleton {
                val rootTmpDir = instance<File>(tag = DiTag.TAG_TMP_DIR)
                BlobUploadServerUseCase(
                    httpCache = instance(),
                    tmpDir = Path(File(rootTmpDir, "blob-upload-server").absolutePath),
                    json = instance(),
                    saveLocalUrisAsBlobsUseCase = instance()
                )
            }

            bind<CreateCacheLocksForActiveContentEntryVersionUseCase>() with scoped(node.endpointScope).singleton {
                CreateCacheLocksForActiveContentEntryVersionUseCase(
                    db = instance(tag = DoorTag.TAG_DB),
                    json = instance(),
                    httpClient = instance(),
                    endpoint = context,
                    createRetentionLocksForManifestUseCase = instance()
                ).also {
                    diToClose.add(it)
                }
            }

            registerContextTranslator { call: ApplicationCall ->
                Endpoint(call.request.headers.asIHttpHeaders().clientProtocolAndHost())
            }

            onReady {
                val testEndpoint = Endpoint("http://localhost:$port/")
                on(testEndpoint).instance<CreateCacheLocksForActiveContentEntryVersionUseCase>()
            }
        }

        ktorServer = embeddedServer(Netty, port) {
            install(ContentNegotiation) {
                json(json = node.di.direct.instance())
            }
            install(CallLogging)

            di {
                extend(di)
            }

            val interceptorVal = ktorInterceptor
            if(interceptorVal != null) {
                intercept(ApplicationCallPipeline.Setup, interceptorVal)
            }

            routing {
                route("api") {
                    val di: DI by closestDI()

                    route("blob") {
                        BlobUploadServerRoute(
                            useCase = { call ->
                                di.on(call).direct.instance()
                            }
                        )

                        CacheRoute(
                            cache = node.httpCache
                        )
                    }
                }

                route("UmAppDatabase") {
                    UmAppDatabase_KtorRoute(
                        DoorHttpServerConfig(json = di.direct.instance(), logger = NapierDoorLogger())
                    ) { call ->
                        di.on(call).direct.instance(tag = DoorTag.TAG_DB)
                    }
                }
            }
        }

        ktorServer.start()
    }

    fun close() {
        diToClose.forEach { it.close() }
        ktorServer.stop()
        node.close()
    }

}