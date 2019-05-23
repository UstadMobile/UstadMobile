package sample

actual class HelloUstadSample {
    actual fun printUstadName() {
        console.log("UstadLog", "Hello Ustad mobile")
    }

    actual fun getUstadName(): String {
        return "Name printed from JS side"
    }

}