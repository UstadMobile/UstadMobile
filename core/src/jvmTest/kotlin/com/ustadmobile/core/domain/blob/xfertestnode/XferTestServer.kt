package com.ustadmobile.core.domain.blob.xfertestnode

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.blob.upload.BlobUploadServerUseCase
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.lib.rest.CacheRoute
import com.ustadmobile.lib.rest.api.blob.BlobUploadServerRoute
import com.ustadmobile.lib.rest.ext.clientProtocolAndHost
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
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
import java.io.File

/**
 * Contains the dependencies required to act as a server for blob transfer testing, including a
 * KTOR server and the required Route(s) to receive blob uploads.
 */
class XferTestServer(
    val node: XferTestNode,
    val port: Int = 8094,
) {

    private val ktorServer: ApplicationEngine

    init {
        ktorServer = embeddedServer(Netty, port) {
            install(ContentNegotiation) {
                json(json = node.di.direct.instance())
            }

            di {
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

                registerContextTranslator {call: ApplicationCall ->
                    Endpoint(call.request.clientProtocolAndHost())
                }
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
            }
        }

        ktorServer.start()
    }

    fun close() {
        ktorServer.stop()
        node.close()
    }

}