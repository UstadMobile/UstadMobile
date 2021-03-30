import react.dom.render
import kotlinx.browser.document
import kotlinx.browser.window
import styled.styledDiv

fun main() {
    window.onload = {
        render(document.getElementById("root")) {
            styledDiv {
                +"UstadMobile app running..."
            }
        }
    }
}
