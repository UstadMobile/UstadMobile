# Ustad Mobile Android module

This provides the app for Android. Fragments, activities, services, etc. are here. This is then 
used as the main dependency for app-android-launcher that provides the APK. It could in theory
be used as a dependency in another app.

### To build

(run from root project directory):

```
 $ ./gradlew ':app-android:assembleDebug'
```

# Android Offline API usage

The Android offline API uses intents and a bound service to allow other apps to authenticate users,
request an authentication token, and then access user data without requiring an Internet connection.
The token generated can be verified online by backend servers after the client reconnects and syncs.

## Authentication

* The caller app sends an intent
```
val authIntent = Intent("com.ustadmobile.AUTH_GET_TOKEN", Uri.parse("local-auth://[server-url]")
```
server-url is optional. If server-url is omitted, then authenticator may prompt the user to select
a server.

* The Ustad app will allow the user to select an account, and will then ask the user if they wish
  to grant permission to the caller app.

* If the user accepts to grant permission to the caller app, the caller receives the result:

```
val accountName = resultIntent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
val accountType = resultIntent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
val authToken = resultIntent.getStringExtra(AccountManager.KEY_AUTHTOKEN)
```

This can be handled as an ActivityResultContract as follows:

```
data class GetTokenResult(
    val resultCode: Int,
    val accountName: String?,
    val accountType: String?,
    val authToken: String?
)

class GetOfflineAuthActivityResultContract: ActivityResultContract<String?, GetTokenResult>() {
    /**
     * @param input where the desired endpoint servername is known, it can provided.
     */
    override fun createIntent(context: Context, input: String?): Intent {
        return Intent("com.ustadmobile.AUTH_GET_TOKEN",
            Uri.parse("local-auth://${input ?: ""}"))
    }

    override fun parseResult(resultCode: Int, intent: Intent?): GetTokenResult {
        val addedName = intent?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        val addedType = intent?.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
        val authToken = intent?.getStringExtra(AccountManager.KEY_AUTHTOKEN)

        return GetTokenResult(resultCode, addedName, addedType, authToken)
    }
}
```
