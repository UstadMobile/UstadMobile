import com.ccfraser.muirwik.components.mThemeProvider
import com.ustadmobile.core.util.defaultJsonSerializer
import com.ustadmobile.util.StateManager
import com.ustadmobile.util.UmTheme
import com.ustadmobile.view.showPreload
import kotlinx.browser.document
import kotlinx.browser.window
import react.dom.render
import react.redux.provider

fun main() {
    defaultJsonSerializer()
    window.onload = {
        render(document.getElementById("root")) {
            provider(StateManager.getStore()){
                mThemeProvider(UmTheme.getUmTheme()) {
                    showPreload()
                }
            }
        }
    }
}
