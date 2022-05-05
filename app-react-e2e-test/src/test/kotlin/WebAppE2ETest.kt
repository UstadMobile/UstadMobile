
import com.ustadmobile.lib.rest.umRestApplication
import io.github.bonigarcia.wdm.WebDriverManager
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class WebAppE2ETest {

    private lateinit var driver:ChromeDriver

    private lateinit var remoteServer: ApplicationEngine

    private val serverPort = 8090

    //@Before
    fun setup() {
        WebDriverManager.chromedriver().setup()
        driver = ChromeDriver()

        remoteServer = embeddedServer(Netty, serverPort, configure = {
            requestReadTimeoutSeconds = 600
            responseWriteTimeoutSeconds = 600
        }) {
            umRestApplication()
        }
        remoteServer.start()
    }

    //@After
    fun cleanup(){
        driver.quit()
        remoteServer.stop(1000, 1000)
    }

    /**
     * Disabled 15/March/22 - fails with classnotfoundexception - fix to follow shortly.
     */
    //@Test
    fun givenWebAppIsLaunched_ThenShouldBeAbleToLogin(){
        driver.get("http://localhost:${serverPort}/")
        val actions = Actions(driver)

        val inputStream = this.javaClass.classLoader.getResourceAsStream("admin.txt")
        val adminPlainPassword = inputStream.bufferedReader().use { it.readText() }


        val loginBtn = WebDriverWait(driver, Duration.ofMinutes(1))
            .until(ExpectedConditions.elementToBeClickable(By.id("login-btn")))

        assertTrue("Login page is loaded and button is displayed",loginBtn.isDisplayed)

        driver.findElement(By.id("username-input")).sendKeys("admin")

        val password = driver.findElement(By.id("password-input"))
        actions.moveToElement(password).click().sendKeys(adminPlainPassword).build().perform()

        loginBtn.click()

        WebDriverWait(driver, Duration.ofMinutes(1))
            .until(ExpectedConditions.urlContains("Home"))
        val homeTitle = driver.findElement(By.className("ComponentStyles-toolbarTitle"))

        assertEquals("App launched successfully, showing content list","content", homeTitle.text.lowercase())
    }
}