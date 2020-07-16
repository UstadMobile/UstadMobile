package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.H5PImportData
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.server.engine.ApplicationEngine
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.Okio
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import com.ustadmobile.util.test.AbstractImportLinkTest
import io.ktor.client.statement.HttpStatement
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TestH5PImportRoute : AbstractImportLinkTest() {

    lateinit var server: ApplicationEngine

    lateinit var db: UmAppDatabase

    var mockServer = MockWebServer()

    internal val dispatcher: Dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {

            try {

                if (request.path.contains("json")) {

                    val fileName = request.path.substring(5)
                    val body = IOUtils.toString(javaClass.getResourceAsStream(fileName), "UTF-8")
                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", "UTF-8".hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(body)

                    return response

                } else if (request.path.contains("content")) {

                    val fileLocation = request.path.substring(8
                    )
                    val videoIn = javaClass.getResourceAsStream(fileLocation)
                    val source = Okio.buffer(Okio.source(videoIn!!))
                    val buffer = Buffer()
                    source.readAll(buffer)

                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", (buffer.size().toString() + "ABC").hashCode())
                    response.setHeader("Content-Type", if (fileLocation.endsWith(".mp4")) "video/mp4" else "text/html")
                    response.setHeader("Content-Length", buffer.size())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.body = buffer

                    return response

                }

            } catch (e: IOException) {
                e.printStackTrace()
            }

            return MockResponse().setResponseCode(404)
        }
    }


    @Before
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, "UmAppDatabase").build()
        db.clearAllTables()
        server = createServer(db, counter)
        createDb(db)
    }

    var count = 0
    val counter = { value: String, uid: Long, content: String, cUid: Long ->
        Unit
        count++
        Unit
    }


    @After
    fun tearDown() {
        server.stop(0, 5000)
        mockServer.shutdown()
    }

    @Test
    fun giveh5pUrlAndContentEntry_whenUrlIsInvalid_thenReturnBadRequestWithMessageInvalid() {
        runBlocking {
            val httpClient = HttpClient() {
                install(JsonFeature)
            }

            val response = httpClient.get<HttpStatement>("http://localhost:8096/ImportH5P/importUrl") {
                parameter("hp5Url", "")
                parameter("parentUid", -1)
            }.execute()

            Assert.assertEquals("Bad Request", 400, response.status.value)

        }


    }

    @Test
    fun giveh5pUrlAndContentEntry_whenUrlIsNotH5P_thenReturnBadRequestWithMessageUnSupported() {
        runBlocking {
            val httpClient = HttpClient() {
                install(JsonFeature)
            }

            mockServer.enqueue(MockResponse().setBody("no h5p here").setResponseCode(200))
            mockServer.start()

            val response = httpClient.get<HttpStatement>("http://localhost:8096/ImportH5P/importUrl") {
                parameter("hp5Url", mockServer.url("/nohp5here").toString())
                parameter("parentUid", -1)
            }.execute()

            Assert.assertEquals("Unsupported Content", 415, response.status.value)
        }


    }

    @Test
    fun giveh5pUrlAndContentEntry_whenUrlIsH5P_thenReturnValid() {

        runBlocking {
            val httpClient = HttpClient {
                install(JsonFeature)
            }
            mockServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
            mockServer.start()


            val response = httpClient.get<HttpStatement>("http://localhost:8096/ImportH5P/importUrl") {
                parameter("hp5Url", mockServer.url("/somehp5here").toString())
                parameter("parentUid", -1)
            }.execute()

            val content = response.receive<H5PImportData>()

            Assert.assertEquals("Content Valid Status 200", 200, response.status.value)
            Assert.assertEquals("Func for h5p download called", 1, count)
            Assert.assertTrue("got the data", content != null)

        }

    }


    @Test
    fun givenH5pContent_whenValidH5p_downloadContentHasAllFiles() {

        runBlocking {

            mockServer.setDispatcher(dispatcher)
            mockServer.start(8097)

            val parent = Files.createTempDirectory("h5p").toFile()

            val body = IOUtils.toString(javaClass.getResourceAsStream("/com/ustadmobile/lib/rest/h5pimportroute/busyants.html"), "UTF-8")

            val contentEntry = ContentEntry()
            contentEntry.contentEntryUid = -300
            db.contentEntryDao.insert(contentEntry)

            val container = Container()
            container.containerContentEntryUid = contentEntry.contentEntryUid
            container.mimeType = "text/html"
            container.fileSize = 11
            container.cntLastModified = 1212
            container.mobileOptimized = true
            container.containerUid = db.containerDao.insert(container)


            downloadH5PUrl(db, "", -300, parent, body, container.containerUid)

            Assert.assertEquals("content size matches container Entry ", 4, db.containerEntryDao.findByContainer(container.containerUid).size)

        }

    }


    @Test
    fun givenValidH5P_whenContentDownloaded_checkContainerIsCreated() {

        GlobalScope.launch {
            mockServer.setDispatcher(dispatcher)
            mockServer.start()

            val parent = Files.createTempDirectory("h5p").toFile()

            val body = IOUtils.toString(javaClass.getResourceAsStream("/com/ustadmobile/lib/rest/h5pimportroute/h5pcontent"), "UTF-8")

            val contentEntry = ContentEntry()
            contentEntry.contentEntryUid = -300
            db.contentEntryDao.insert(contentEntry)

            val container = Container()
            container.containerContentEntryUid = contentEntry.contentEntryUid
            container.mimeType = "text/html"
            container.fileSize = 11
            container.cntLastModified = 1212
            container.mobileOptimized = true
            container.containerUid = db.containerDao.insert(container)


            downloadH5PUrl(db, mockServer.url("/json/com/ustadmobile/lib/rest/h5pimportroute/h5pcontent").toString(), -300, parent, body, container.containerUid)

            val containerDb = db.containerDao.getMostRecentContainerForContentEntry(-300)

            Assert.assertTrue("index.json exists", File(parent, "index.json").exists())
            Assert.assertTrue("contentEntry has container", containerDb != null)
            Assert.assertTrue("", db.containerEntryDao.findByContainer(container!!.containerUid).map { it.cePath }.contains("index.json"))
        }

    }

    @Test
    fun giveVideoUrlAndContentEntry_whenUrlFileSizeTooBig_thenReturnBadRequestWithMessageInvalid() {
        runBlocking {
            val httpClient = HttpClient() {
                install(JsonFeature)
            }

            mockServer.enqueue(MockResponse().addHeader("Content-Type", "video/mp4").setHeader("Content-Length", 104857600).setResponseCode(200))
            mockServer.start()


            val response = httpClient.get<HttpStatement>("http://localhost:8096/ImportH5P/importVideo") {
                parameter("hp5Url", mockServer.url("").toString())
                parameter("parentUid", -1)
                parameter("title", "Video Title")
            }.execute()

            Assert.assertEquals("Bad Request", 400, response.status.value)

        }

    }

    @Test
    fun giveVideoUrlAndContentEntry_whenContentTypeIsNotVideo_thenReturnBadRequestWithMessageInvalid() {
        runBlocking {
            val httpClient = HttpClient() {
                install(JsonFeature)
            }

            mockServer.enqueue(MockResponse().addHeader("Content-Type", "text/html").setResponseCode(200))
            mockServer.start()


            val response = httpClient.get<HttpStatement>("http://localhost:8096/ImportH5P/importVideo") {
                parameter("hp5Url", mockServer.url("").toString())
                parameter("parentUid", -1)
                parameter("title", "Video Title")
            }.execute()

            Assert.assertEquals("Bad Request", 400, response.status.value)

        }

    }


    @Test
    fun giveVideoUrlAndContentEntry_whenValid_thenReturnH5PImport() {
        runBlocking {
            val httpClient = HttpClient() {
                install(JsonFeature)
            }
            mockServer.setDispatcher(dispatcher)
            mockServer.start()

            val response = httpClient.get<H5PImportData>("http://localhost:8096/ImportH5P/importVideo") {
                parameter("hp5Url", mockServer.url("/content/com/ustadmobile/lib/rest/h5pimportroute/video.mp4").toString())
                parameter("parentUid", -100)
                parameter("title", "Video Title")
            }

            val containerDb = db.containerDao.getMostRecentContainerForContentEntry(response.contentEntry.contentEntryUid)
            Assert.assertTrue("contentEntry has container", containerDb != null)
            Assert.assertTrue("", db.containerEntryDao.findByContainer(response.container.containerUid).map { it.cePath }.contains("video.mp4"))

        }

    }

    @Test
    fun givenExistingContentEntry_whenValid_thenCheckContentEntryUpdated() {

        runBlocking {

            val httpClient = HttpClient {
                install(JsonFeature)
            }
            mockServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
            mockServer.start()

            val contentEntry = ContentEntry()
            contentEntry.title = "test"
            contentEntry.leaf = true
            contentEntry.sourceUrl = "urlbefore"
            contentEntry.contentEntryUid = -2

            val response = httpClient.get<HttpStatement>("http://localhost:8096/ImportH5P/importUrl") {
                parameter("hp5Url", mockServer.url("/somehp5here").toString())
                parameter("parentUid", -1)
                parameter("contentEntryUid", contentEntry.contentEntryUid)
            }.execute()

            val content = response.receive<H5PImportData>()

            Assert.assertEquals("Content Valid Status 200", 200, response.status.value)
            Assert.assertEquals("Func for h5p download called", 1, count)
            Assert.assertTrue("got the data", content != null)
            Assert.assertEquals("source url got updated", "http://localhost:${mockServer.port}/somehp5here", content.contentEntry.sourceUrl)


        }


    }


    @Test
    fun findLinks() {
        var string = "{\"l10n\":{\"slide\":\"Slide\",\"yourScore\":\"Your Score\",\"maxScore\":\"Max Score\",\"showSolutions\":\"Show solutions\",\"exportAnswers\":\"Export text\",\"hideKeywords\":\"Hide keywords list\",\"showKeywords\":\"Show keywords list\",\"fullscreen\":\"fullscreen\",\"exitFullscreen\":\"Exit fullscreen\",\"prevSlide\":\"Previous slide\",\"nextSlide\":\"Next slide\",\"currentSlide\":\"Current slide\",\"lastSlide\":\"Last slide\",\"solutionModeTitle\":\"Exit solution mode\",\"solutionModeText\":\"Solution Mode\",\"summaryMultipleTaskText\":\"Text when multiple tasks on a page\",\"scoreMessage\":\"You achieved:\",\"shareFacebook\":\"Share on Facebook\",\"shareTwitter\":\"Share on Twitter\",\"retry\":\"Retry\",\"summary\":\"Summary\",\"solutionsButtonTitle\":\"Show comments\",\"printTitle\":\"Print\",\"printIngress\":\"How would you like to print this presentation?\",\"printAllSlides\":\"Print all slides\",\"printCurrentSlide\":\"Print current slide\",\"total\":\"TOTAL\",\"shareGoogle\":\"Share on Google+\",\"noTitle\":\"No title\",\"accessibilitySlideNavigationExplanation\":\"Use left and right arrow to change slide in that direction whenever canvas is selected.\",\"accessibilityCanvasLabel\":\"Presentation canvas. Use left and right arrow to move between slides.\",\"containsNotCompleted\":\"@slideName contains not completed interaction\",\"containsCompleted\":\"@slideName contains completed interaction\",\"slideCount\":\"Slide @index of @total\",\"containsOnlyCorrect\":\"@slideName only has correct answers\",\"containsIncorrectAnswers\":\"@slideName has incorrect answers\",\"score\":\"Score\",\"totalScore\":\"Total Score\",\"shareResult\":\"Share Result\",\"accessibilityTotalScore\":\"You got @score of @maxScore points in total\"},\"override\":{\"hideSummarySlide\":false,\"activeSurface\":false,\"showSolutionButton\":\"on\",\"retryButton\":\"on\",\"enablePrintButton\":true,\"social\":{\"showFacebookShare\":true,\"facebookShare\":{\"url\":\"@currentpageurl\",\"quote\":\"I scored @percentage on a task at @currentpageurl.\"},\"showTwitterShare\":false,\"twitterShare\":{\"statement\":\"I scored @percentage on a task at @currentpageurl.\",\"url\":\"@currentpageurl\",\"hashtags\":\"h5p, course\"},\"showGoogleShare\":false,\"googleShareUrl\":\"@currentpageurl\"}},\"presentation\":{\"slides\":[{\"elements\":[{\"x\":79.617834394904,\"y\":88.050314465409,\"width\":18.66557954412,\"height\":7.2779305945902,\"action\":{\"library\":\"H5P.AdvancedText 1.1\",\"params\":{\"text\":\"<p>&nbsp;Jump to redcurrant<\\/p>\\n\"},\"subContentId\":\"56389ab2-a70f-4265-9099-d28023126dd8\",\"metadata\":{\"contentType\":\"Text\",\"license\":\"U\",\"title\":\"Untitled Text\"}},\"alwaysDisplayComments\":false,\"backgroundOpacity\":50,\"displayAsButton\":false,\"invisible\":false,\"solution\":\"\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"},{\"action\":{\"library\":\"H5P.AdvancedText 1.1\",\"params\":{\"text\":\"<h2><strong>Cloudberries<\\/strong><\\/h2>\\n\\n<ul>\\n\\t<li>\\n\\t<p>Cloudberries grow in alpine and arctic tundra.<\\/p>\\n\\t<\\/li>\\n\\t<li>\\n\\t<p>The cloudberry is also known as bakeapple, knotberry and averin, and is part of the Rose family.&nbsp;<\\/p>\\n\\t<\\/li>\\n\\t<li>\\n\\t<p>Each fruit is initially pale red, ripening into an amber color in early autumn.<\\/p>\\n\\t<\\/li>\\n\\t<li>\\n\\t<p>Scandinavia has strict rules for harvesting cloudberries. Sweden even has a section for regulating this in their Ministry of Foreign Affairs.<\\/p>\\n\\t<\\/li>\\n<\\/ul>\\n\"},\"subContentId\":\"2a8c73bd-0a24-4896-915f-d552d7c40f52\",\"metadata\":{\"contentType\":\"Text\",\"license\":\"U\",\"title\":\"Untitled Text\"}},\"x\":2.1786492374728,\"y\":4.2831612903226,\"width\":46.949891067538,\"height\":89.89247311828,\"alwaysDisplayComments\":false,\"backgroundOpacity\":60,\"displayAsButton\":false,\"solution\":\"\",\"invisible\":false,\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"},{\"x\":79.617834394904,\"y\":69.182389937107,\"width\":18.683651804671,\"height\":19.077568134172,\"action\":{\"library\":\"H5P.Image 1.1\",\"params\":{\"contentName\":\"Image\",\"file\":{\"path\":\"images\\/file-550ff73380390.jpg\",\"mime\":\"image\\/jpeg\",\"width\":1600,\"height\":800,\"copyright\":{\"license\":\"U\"}},\"alt\":\"Photo of red currants\",\"title\":\"Red currants\"},\"subContentId\":\"266a00e6-a02d-4ac1-920a-d1acf2e4a423\",\"metadata\":{\"license\":\"U\",\"contentType\":\"Image\",\"title\":\"Untitled Image\"}},\"alwaysDisplayComments\":false,\"backgroundOpacity\":0,\"displayAsButton\":false,\"invisible\":false,\"solution\":\"\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"},{\"x\":79.520697167756,\"y\":68.817204301075,\"width\":18.683651804671,\"height\":26.205450733753,\"goToSlide\":4,\"backgroundOpacity\":0,\"displayAsButton\":false,\"invisible\":false,\"title\":\"Skip to redcurrant\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"}],\"slideBackgroundSelector\":{},\"keywords\":[{\"main\":\"Cloudberries\"}]},{\"elements\":[{\"x\":2.1786492374728,\"y\":4.3010752688172,\"width\":64.968152866242,\"height\":59.119496855346,\"action\":{\"library\":\"H5P.SingleChoiceSet 1.11\",\"params\":{\"choices\":[{\"answers\":[\"amber\",\"pale red\",\"black\",\"brown\"],\"question\":\"<p>What color do ripe cloudberries have?<\\/p>\\n\",\"subContentId\":\"0a5f12b1-c025-4a19-9771-d31d58511438\"},{\"answers\":[\"Sweden\",\"France\",\"Germany\",\"India\"],\"question\":\"What country has a special section of cloudberry diplomacy in their Ministry of Foreign Affairs\",\"subContentId\":\"bb04138f-ecf9-45ea-a2ac-735f8797e7a9\"},{\"answers\":[\"Rose family\",\"Magnolia family\",\"Oak and beech family\",\"Grass family\"],\"question\":\"What family of plants does cloudberry belong to ?\",\"subContentId\":\"5bbb8c42-699a-4d4d-aa4e-5dd8f876bf4a\"}],\"behaviour\":{\"timeoutCorrect\":0,\"timeoutWrong\":0,\"soundEffectsEnabled\":true,\"enableRetry\":true,\"enableSolutionsButton\":true,\"passPercentage\":100,\"autoContinue\":true},\"l10n\":{\"showSolutionButtonLabel\":\"Show solution\",\"retryButtonLabel\":\"Retry\",\"solutionViewTitle\":\"Solution\",\"correctText\":\"Correct!\",\"incorrectText\":\"Incorrect!\",\"muteButtonLabel\":\"Mute feedback sound\",\"closeButtonLabel\":\"Close\",\"slideOfTotal\":\"Slide :num of :total\",\"nextButtonLabel\":\"Next question\",\"scoreBarLabel\":\"You got :num out of :total points\"},\"overallFeedback\":[{\"from\":0,\"to\":100,\"feedback\":\"You got :numcorrect of :maxscore correct\"}]},\"subContentId\":\"8964f94f-190c-4fe7-a729-3c89618b9fb0\",\"metadata\":{\"title\":\"What color do ripe cloudberries have?\",\"license\":\"U\",\"contentType\":\"Single Choice Set\"}},\"alwaysDisplayComments\":false,\"backgroundOpacity\":60,\"displayAsButton\":false,\"invisible\":false,\"solution\":\"\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"}],\"slideBackgroundSelector\":{},\"keywords\":[{\"main\":\"Cloudberries task 1\"}]},{\"elements\":[{\"action\":{\"library\":\"H5P.Blanks 1.11\",\"params\":{\"questions\":[\"<p>The cloudberry is also known as knotberry, bakeapple or *averin*.<\\/p>\\n\",\"<p>Cloudberries can be found in alpine and *arctic* tundra.<\\/p>\\n\"],\"showSolutions\":\"Show solutions\",\"tryAgain\":\"Retry\",\"checkAnswer\":\"Check\",\"notFilledOut\":\"Please fill in all blanks\",\"text\":\"<p>Fill in the blanks<\\/p>\\n\",\"behaviour\":{\"enableSolutionsButton\":true,\"autoCheck\":false,\"caseSensitive\":true,\"showSolutionsRequiresInput\":true,\"separateLines\":false,\"enableRetry\":true,\"disableImageZooming\":false,\"confirmCheckDialog\":false,\"confirmRetryDialog\":false,\"acceptSpellingErrors\":false,\"enableCheckButton\":true},\"answerIsCorrect\":\"&#039;:ans&#039; is correct\",\"answerIsWrong\":\"&#039;:ans&#039; is wrong\",\"answeredCorrectly\":\"Answered correctly\",\"answeredIncorrectly\":\"Answered incorrectly\",\"solutionLabel\":\"Correct answer:\",\"inputLabel\":\"Blank input @num of @total\",\"inputHasTipLabel\":\"Tip available\",\"tipLabel\":\"Tip\",\"confirmCheck\":{\"header\":\"Finish ?\",\"body\":\"Are you sure you wish to finish ?\",\"cancelLabel\":\"Cancel\",\"confirmLabel\":\"Finish\"},\"confirmRetry\":{\"header\":\"Retry ?\",\"body\":\"Are you sure you wish to retry ?\",\"cancelLabel\":\"Cancel\",\"confirmLabel\":\"Confirm\"},\"overallFeedback\":[{\"from\":0,\"to\":100,\"feedback\":\"You got @score of @total blanks correct.\"}],\"scoreBarLabel\":\"You got :num out of :total points\"},\"subContentId\":\"a648de70-c1fd-4692-9261-ce44bc0a9204\",\"metadata\":{\"title\":\"Fill in the blanks\",\"license\":\"U\",\"contentType\":\"Fill in the Blanks\"}},\"x\":2.1786492374728,\"y\":4.2921075268817,\"width\":39.324618736383,\"height\":89.89247311828,\"alwaysDisplayComments\":false,\"backgroundOpacity\":60,\"displayAsButton\":false,\"solution\":\"\",\"invisible\":false,\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"}],\"slideBackgroundSelector\":{},\"keywords\":[{\"main\":\"Cloudberries task 2\"}]},{\"elements\":[{\"x\":2.1231422505308,\"y\":62.893081761006,\"width\":19.32059447983,\"height\":27.882599580713,\"action\":{\"library\":\"H5P.Image 1.1\",\"params\":{\"contentName\":\"Image\",\"file\":{\"path\":\"images\\/file-566829bd830c1.jpg\",\"mime\":\"image\\/jpeg\",\"width\":1001,\"height\":730,\"copyright\":{\"license\":\"U\"}},\"alt\":\"A photo of blueberries\",\"title\":\"Blueberries\"},\"subContentId\":\"811188cd-fa0e-477a-8753-dd51918f2f94\",\"metadata\":{\"license\":\"U\",\"contentType\":\"Image\",\"title\":\"Untitled Image\"}},\"alwaysDisplayComments\":false,\"backgroundOpacity\":0,\"displayAsButton\":false,\"invisible\":false,\"solution\":\"\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"},{\"action\":{\"library\":\"H5P.AdvancedText 1.1\",\"params\":{\"text\":\"<h2><strong>Redcurrant<\\/strong><\\/h2>\\n\\n<ul>\\n\\t<li>\\n\\t<p><em>Ribes Rubrum <\\/em>is the latin name for the redcurrant<\\/p>\\n\\t<\\/li>\\n\\t<li>\\n\\t<p>The flowers of the shrub mature into red translucent berries<\\/p>\\n\\t<\\/li>\\n\\t<li>\\n\\t<p>Berries are 8\\u201312 mm in diameter and have a tart flavour<\\/p>\\n\\t<\\/li>\\n\\t<li>\\n\\t<p>Each bush can produce 3-4 kilos of berries each season<\\/p>\\n\\t<\\/li>\\n<\\/ul>\\n\"},\"subContentId\":\"893f0076-5864-4683-a650-51c8982624c9\",\"metadata\":{\"contentType\":\"Text\",\"license\":\"U\",\"title\":\"Untitled Text\"}},\"x\":53.376906318083,\"y\":6.4516129032258,\"width\":42.95,\"height\":88.699588477366,\"alwaysDisplayComments\":false,\"backgroundOpacity\":60,\"displayAsButton\":false,\"solution\":\"\",\"invisible\":false,\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"},{\"x\":2.1231422505308,\"y\":90.146750524109,\"width\":19.32059447983,\"height\":7.5471698113208,\"action\":{\"library\":\"H5P.AdvancedText 1.1\",\"params\":{\"text\":\"<p>Jump to blueberries<\\/p>\\n\"},\"subContentId\":\"b3ecd51f-810a-420b-aa1b-03bba7777e5e\",\"metadata\":{\"contentType\":\"Text\",\"license\":\"U\",\"title\":\"Untitled Text\"}},\"alwaysDisplayComments\":false,\"backgroundOpacity\":50,\"displayAsButton\":false,\"invisible\":false,\"solution\":\"\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"},{\"x\":2.1786492374728,\"y\":63.010752688172,\"width\":19.32059447983,\"height\":34.800838574423,\"goToSlide\":7,\"backgroundOpacity\":0,\"displayAsButton\":false,\"invisible\":false,\"title\":\"Blueberries\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"}],\"slideBackgroundSelector\":{\"imageSlideBackground\":{\"path\":\"images\\/imageSlideBackground-56c5a34a4470f.jpg\",\"mime\":\"image\\/jpeg\",\"copyright\":{\"license\":\"CC BY-SA\",\"title\":\"Ribes_rubrum2005-07-17.JPG\",\"author\":\"Luke1ace\",\"source\":\"http:\\/\\/commons.wikimedia.org\\/wiki\\/File:Ribes_rubrum2005-07-17.JPG\",\"version\":\"4.0\"},\"width\":1600,\"height\":800}},\"keywords\":[{\"main\":\"Redcurrant\"}]},{\"elements\":[{\"action\":{\"library\":\"H5P.AdvancedText 1.1\",\"params\":{\"text\":\"<h2><strong>Here's a video about growing currants!<\\/strong><\\/h2>\\n\"},\"subContentId\":\"cb8280d5-2cbc-48a1-b855-384929fae6ae\",\"metadata\":{\"contentType\":\"Text\",\"license\":\"U\",\"title\":\"Untitled Text\"}},\"x\":16.339869281046,\"y\":6.4516129032258,\"width\":67.320261437908,\"height\":89.89247311828,\"alwaysDisplayComments\":false,\"backgroundOpacity\":50,\"displayAsButton\":false,\"solution\":\"\",\"invisible\":false,\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"},{\"action\":{\"library\":\"H5P.Video 1.5\",\"params\":{\"l10n\":{\"name\":\"Video\",\"loading\":\"Video player loading...\",\"noPlayers\":\"Found no video players that supports the given video format.\",\"noSources\":\"Video is missing sources.\",\"aborted\":\"Media playback has been aborted.\",\"networkFailure\":\"Network failure.\",\"cannotDecode\":\"Unable to decode media.\",\"formatNotSupported\":\"Video format not supported.\",\"mediaEncrypted\":\"Media encrypted.\",\"unknownError\":\"Unknown error.\",\"invalidYtId\":\"Invalid YouTube ID.\",\"unknownYtId\":\"Unable to find video with the given YouTube ID.\",\"restrictedYt\":\"The owner of this video does not allow it to be embedded.\"},\"sources\":[{\"path\":\"https:\\/\\/www.youtube.com\\/watch?v=r3nqoR3RlJY\",\"mime\":\"video\\/YouTube\",\"copyright\":{\"license\":\"U\"}}],\"visuals\":{\"fit\":true,\"controls\":true},\"playback\":{\"autoplay\":true,\"loop\":false}},\"subContentId\":\"6683e5b8-49df-4b9a-afdb-403549cd0350\",\"metadata\":{\"title\":\"Growing Currants\",\"authors\":[{\"name\":\"GrowOrganic Peaceful Valley\",\"role\":\"Author\"}],\"source\":\"https:\\/\\/youtu.be\\/r3nqoR3RlJY\",\"license\":\"U\",\"contentType\":\"Video\"}},\"x\":19.108280254777,\"y\":20.964360587002,\"width\":61.458333333333,\"height\":68.312757201646,\"alwaysDisplayComments\":false,\"backgroundOpacity\":0,\"displayAsButton\":false,\"solution\":\"\",\"invisible\":false,\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"}],\"slideBackgroundSelector\":{\"imageSlideBackground\":{\"path\":\"images\\/imageSlideBackground-56c5a34e5743e.jpg\",\"mime\":\"image\\/jpeg\",\"copyright\":{\"license\":\"U\"},\"width\":1600,\"height\":800}},\"keywords\":[{\"main\":\"Currant video\"}]},{\"elements\":[{\"action\":{\"library\":\"H5P.Summary 1.10\",\"params\":{\"intro\":\"<p>Choose the correct statement.<\\/p>\\n\",\"summaries\":[{\"summary\":[\"<p>Cloudberries are also known as knotberries.<\\/p>\\n\",\"<p>Cloudberries are also known as huckleberries.<\\/p>\\n\",\"<p>Cloudberries are also known as gooseberries.<\\/p>\\n\"],\"tip\":\"\",\"subContentId\":\"59048047-6540-49e4-9763-3cc803fb9cef\"},{\"summary\":[\"<p>Redcurrant bushes can produce 3\\u20134 kilos of berries each season.<\\/p>\\n\",\"<p>Redcurrant bushes can produce 6\\u20137 kilos of berries each season.<\\/p>\\n\",\"<p>Redcurrant bushes can produce 1\\u20132 kilos of berries each season.<\\/p>\\n\"],\"tip\":\"\",\"subContentId\":\"455c769c-8ee4-4219-a342-3cc1d2b9f0b5\"},{\"summary\":[\"<p>Cloudberries mature into an amber color in early autumn.<\\/p>\\n\",\"<p>Cloudberries mature into an amber color in early spring.<\\/p>\\n\",\"<p>Cloudberries mature into an amber color in late winter.<\\/p>\\n\"],\"tip\":\"\",\"subContentId\":\"bdb7a423-285a-44af-bbc1-a81b4f9d70fd\"},{\"summary\":[\"<p>Redcurrant berries have a naturally tart flavour.<\\/p>\\n\",\"<p>Redcurrant berries have a naturally sweet flavour.<\\/p>\\n\",\"<p>Redcurrant berries have a neutral flavour.<\\/p>\\n\"],\"tip\":\"\",\"subContentId\":\"a762c11e-8673-463e-bb90-fa3b29b59337\"}],\"solvedLabel\":\"Progress:\",\"scoreLabel\":\"Wrong answers:\",\"resultLabel\":\"Your result\",\"overallFeedback\":[{\"from\":0,\"to\":100,\"feedback\":\"You got @score of @total statements (@percent %) correct.\"}],\"labelCorrect\":\"Correct.\",\"labelIncorrect\":\"Incorrect! Please try again.\",\"labelCorrectAnswers\":\"Correct answers.\",\"tipButtonLabel\":\"Show tip\",\"scoreBarLabel\":\"You got :num out of :total points\",\"progressText\":\"Progress :num of :total\"},\"subContentId\":\"2ae44093-5173-426c-b5fa-e8fecb3d62fc\",\"metadata\":{\"title\":\"Choose the correct statement.\",\"license\":\"U\",\"contentType\":\"Summary\"}},\"x\":4.3572984749455,\"y\":6.4516129032258,\"width\":89.21875,\"height\":83.641975308642,\"alwaysDisplayComments\":false,\"backgroundOpacity\":60,\"displayAsButton\":false,\"solution\":\"\",\"invisible\":false,\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"}],\"slideBackgroundSelector\":{\"imageSlideBackground\":{\"path\":\"images\\/imageSlideBackground-56c5a3514744b.jpg\",\"mime\":\"image\\/jpeg\",\"copyright\":{\"license\":\"U\"},\"width\":1600,\"height\":800}},\"keywords\":[{\"main\":\"Currant and berry task\"}]},{\"elements\":[{\"x\":3.1847133757962,\"y\":67.085953878407,\"width\":19.32059447983,\"height\":26.205450733753,\"goToSlide\":1,\"backgroundOpacity\":0,\"displayAsButton\":false,\"invisible\":false,\"title\":\"Cloudberry\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"},{\"x\":50.954248366013,\"y\":4.1845806451613,\"width\":47.167755991285,\"height\":89.89247311828,\"action\":{\"library\":\"H5P.AdvancedText 1.1\",\"params\":{\"text\":\"<h2>Blueberry<\\/h2>\\n\\n<ul>\\n\\t<li>Blueberries start out with a greenish color, then turn purple and finally a deep blue as they ripe.<\\/li>\\n\\t<li>Blueberries grow in clusters, but the clusters do not necessarily ripen at the same time.<\\/li>\\n\\t<li>Blueberries require a well-drained, acidic soil to flourish.<\\/li>\\n\\t<li>There are over 50 varieties of blueberries.<\\/li>\\n\\t<li>Blueberries have been commercially cultivated since the 20th century<\\/li>\\n<\\/ul>\\n\"},\"subContentId\":\"e26227bb-ff95-4a94-8434-61a970934b83\",\"metadata\":{\"contentType\":\"Text\",\"license\":\"U\",\"title\":\"Untitled Text\"}},\"alwaysDisplayComments\":false,\"backgroundOpacity\":60,\"displayAsButton\":false,\"invisible\":false,\"solution\":\"\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"},{\"x\":3.2679738562092,\"y\":86.021505376344,\"width\":19.32059447983,\"height\":7.9664570230608,\"action\":{\"library\":\"H5P.AdvancedText 1.1\",\"params\":{\"text\":\"<p>Jump to Cloudberry<\\/p>\\n\"},\"subContentId\":\"b234ffb0-816b-41a0-b8eb-8d7c5a6a81ba\",\"metadata\":{\"contentType\":\"Text\",\"license\":\"U\",\"title\":\"Untitled Text\"}},\"alwaysDisplayComments\":false,\"backgroundOpacity\":60,\"displayAsButton\":false,\"invisible\":false,\"solution\":\"\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"},{\"x\":3.1847133757962,\"y\":67.085953878407,\"width\":19.32059447983,\"height\":19.287211740042,\"action\":{\"library\":\"H5P.Image 1.1\",\"params\":{\"contentName\":\"Image\",\"file\":{\"path\":\"images\\/file-56682ea16678b.jpg\",\"mime\":\"image\\/jpeg\",\"width\":1600,\"height\":800,\"copyright\":{\"license\":\"U\"}},\"alt\":\"A photo of cloudberry\",\"title\":\"Cloudberry\"},\"subContentId\":\"530001c0-5f7c-428a-9503-89045e4e9e59\",\"metadata\":{\"license\":\"U\",\"contentType\":\"Image\",\"title\":\"Untitled Image\"}},\"alwaysDisplayComments\":false,\"backgroundOpacity\":0,\"displayAsButton\":false,\"invisible\":false,\"solution\":\"\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"},{\"x\":3.2679738562092,\"y\":66.666666666667,\"width\":19.32059447983,\"height\":27.253668763103,\"goToSlide\":1,\"backgroundOpacity\":0,\"displayAsButton\":false,\"invisible\":false,\"title\":\"Jump to Cloudberry\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"}],\"slideBackgroundSelector\":{\"imageSlideBackground\":{\"path\":\"images\\/imageSlideBackground-56c5a35570519.jpg\",\"mime\":\"image\\/jpeg\",\"copyright\":{\"license\":\"PD\",\"title\":\"Patts Blueberries\",\"author\":\"PhreddieH3\",\"source\":\"https:\\/\\/commons.wikimedia.org\\/wiki\\/File:PattsBlueberries.jpg\"},\"width\":1600,\"height\":800}},\"keywords\":[{\"main\":\"Blueberry\"}]},{\"elements\":[{\"x\":46.840958605664,\"y\":4.2921075268817,\"width\":50.762527233115,\"height\":89.89247311828,\"action\":{\"library\":\"H5P.DragText 1.8\",\"params\":{\"taskDescription\":\"<p>Drag colors to match the ripening stages.<\\/p>\\n\",\"checkAnswer\":\"Check\",\"tryAgain\":\"Retry\",\"showSolution\":\"Show Solution\",\"behaviour\":{\"enableRetry\":true,\"enableSolutionsButton\":true,\"instantFeedback\":false,\"enableCheckButton\":true},\"textField\":\"Blueberries begin with a *green* color.\\nAs they ripen, the berries turn *purple*, then gradually acquire a deep *blue* color.\",\"overallFeedback\":[{\"from\":0,\"to\":100,\"feedback\":\"You got @score of @total blanks correct.\"}],\"dropZoneIndex\":\"Drop Zone @index.\",\"empty\":\"Drop Zone @index is empty.\",\"contains\":\"Drop Zone @index contains draggable @draggable.\",\"draggableIndex\":\"Draggable @text. @index of @count draggables.\",\"tipLabel\":\"Show tip\",\"correctText\":\"Correct!\",\"incorrectText\":\"Incorrect!\",\"resetDropTitle\":\"Reset drop\",\"resetDropDescription\":\"Are you sure you want to reset this drop zone?\",\"grabbed\":\"Draggable is grabbed.\",\"cancelledDragging\":\"Cancelled dragging.\",\"correctAnswer\":\"Correct answer:\",\"feedbackHeader\":\"Feedback\",\"scoreBarLabel\":\"You got :num out of :total points\"},\"subContentId\":\"1ea67977-bd51-42ad-acc3-d68581bf0687\",\"metadata\":{\"title\":\"Drag colors to match the ripening stages.\",\"license\":\"U\",\"contentType\":\"Drag Text\"}},\"alwaysDisplayComments\":false,\"backgroundOpacity\":60,\"displayAsButton\":false,\"invisible\":false,\"solution\":\"\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"}],\"slideBackgroundSelector\":{\"imageSlideBackground\":{\"path\":\"images\\/imageSlideBackground-56c5a358dfeeb.jpg\",\"mime\":\"image\\/jpeg\",\"copyright\":{\"license\":\"U\"},\"width\":1600,\"height\":800}},\"keywords\":[{\"main\":\"Blueberry task\"}]},{\"elements\":[{\"x\":2.1230501089325,\"y\":4.1733978494624,\"width\":48.80174291939,\"height\":92.043010752688,\"action\":{\"library\":\"H5P.MultiChoice 1.13\",\"params\":{\"answers\":[{\"correct\":false,\"tipsAndFeedback\":{\"chosenFeedback\":\"\",\"notChosenFeedback\":\"\",\"tip\":\"\"},\"text\":\"<div>Less than 10<\\/div>\\n\"},{\"correct\":true,\"tipsAndFeedback\":{\"chosenFeedback\":\"\",\"notChosenFeedback\":\"\",\"tip\":\"\"},\"text\":\"<div>More than 50<\\/div>\\n\"},{\"correct\":false,\"tipsAndFeedback\":{\"chosenFeedback\":\"\",\"notChosenFeedback\":\"\",\"tip\":\"\"},\"text\":\"<div>Between 10 and 30<\\/div>\\n\"},{\"correct\":false,\"tipsAndFeedback\":{\"chosenFeedback\":\"\",\"notChosenFeedback\":\"\",\"tip\":\"\"},\"text\":\"<div>Between 30 and 50<\\/div>\\n\"}],\"UI\":{\"checkAnswerButton\":\"Check\",\"showSolutionButton\":\"Show solution\",\"tryAgainButton\":\"Retry\",\"tipsLabel\":\"Show tip\",\"scoreBarLabel\":\"You got :num out of :total points\",\"tipAvailable\":\"Tip available\",\"feedbackAvailable\":\"Feedback available\",\"readFeedback\":\"Read feedback\",\"wrongAnswer\":\"Wrong answer\",\"correctAnswer\":\"Correct answer\",\"shouldCheck\":\"Should have been checked\",\"shouldNotCheck\":\"Should not have been checked\",\"noInput\":\"Please answer before viewing the solution\"},\"behaviour\":{\"enableRetry\":true,\"enableSolutionsButton\":true,\"singlePoint\":true,\"randomAnswers\":true,\"showSolutionsRequiresInput\":true,\"type\":\"auto\",\"disableImageZooming\":false,\"confirmCheckDialog\":false,\"confirmRetryDialog\":false,\"autoCheck\":false,\"passPercentage\":100,\"showScorePoints\":true,\"enableCheckButton\":true},\"question\":\"<p>How many varieties of blueberry exists ?<\\/p>\\n\",\"confirmCheck\":{\"header\":\"Finish ?\",\"body\":\"Are you sure you wish to finish ?\",\"cancelLabel\":\"Cancel\",\"confirmLabel\":\"Finish\"},\"confirmRetry\":{\"header\":\"Retry ?\",\"body\":\"Are you sure you wish to retry ?\",\"cancelLabel\":\"Cancel\",\"confirmLabel\":\"Confirm\"},\"overallFeedback\":[{\"from\":0,\"to\":0,\"feedback\":\"Wrong\"},{\"from\":1,\"to\":99,\"feedback\":\"Almost!\"},{\"from\":100,\"to\":100,\"feedback\":\"Correct!\"}]},\"subContentId\":\"94fd99d6-33ec-4ee5-9067-7c6554678d8c\",\"metadata\":{\"title\":\"How many varieties of blueberry exists ?\",\"license\":\"U\",\"contentType\":\"Multiple Choice\"}},\"alwaysDisplayComments\":false,\"backgroundOpacity\":60,\"displayAsButton\":false,\"invisible\":false,\"solution\":\"\",\"buttonSize\":\"big\",\"goToSlideType\":\"specified\"}],\"slideBackgroundSelector\":{\"imageSlideBackground\":{\"path\":\"images\\/imageSlideBackground-56c5a35d31b88.jpg\",\"mime\":\"image\\/jpeg\",\"copyright\":{\"license\":\"U\"},\"width\":1600,\"height\":800}},\"keywords\":[{\"main\":\"Blueberry task 2\"}]}],\"keywordListEnabled\":true,\"keywordListAlwaysShow\":false,\"keywordListAutoHide\":true,\"keywordListOpacity\":90,\"globalBackgroundSelector\":{\"imageGlobalBackground\":{\"path\":\"images\\/imageGlobalBackground-56c5a318a5141.jpg\",\"mime\":\"image\\/jpeg\",\"copyright\":{\"license\":\"CC BY-SA\",\"title\":\"cloudberry-bg.jpg\",\"author\":\"Frode Petterson\",\"source\":\"http:\\/\\/h5p.org\",\"version\":\"4.0\"},\"width\":1600,\"height\":800}}}}"

        val json = Json(JsonConfiguration(ignoreUnknownKeys = false))

        val list = findLinks(json.parseJson(string).jsonObject)

        Assert.assertEquals("list count matches", 11, list.size)

    }


}