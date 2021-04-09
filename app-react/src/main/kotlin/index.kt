import com.ccfraser.muirwik.components.mThemeProvider
import com.ustadmobile.core.util.defaultJsonSerializer
import com.ustadmobile.util.UmTheme
import com.ustadmobile.view.showPreload
import kotlinx.browser.document
import kotlinx.browser.window
import react.dom.render

fun main() {
    defaultJsonSerializer()
    window.onload = {
        render(document.getElementById("root")) {
            mThemeProvider(UmTheme.getUmTheme()) {
                showPreload()
            }
        }
    }
}
