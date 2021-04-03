import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.view.umBaseApp
import kotlinx.browser.document
import kotlinx.browser.window
import react.dom.render

fun main() {
    UstadMobileSystemImpl.instance.registerDefaultSerializer()
    window.onload = {
        render(document.getElementById("root")) {
            umBaseApp()
        }
    }
}
