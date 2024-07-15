
# Android Offline API usage

The Android offline API uses intents and a bound service to allow other apps to authenticate users,
request an authentication token, and then access user data without requiring an Internet connection.

Getting a token (e.g. as would normally be done using OAuth via redirection in the browser) is done
using [start activity for result](https://developer.android.com/training/basics/intents/result) 
where the app requesting a token sends an intent to the app that will issue the token.  

Subsequent API requests using the token are done using Http over IPC where http requests are 
serialized and sent to the app that issued the token using a [Bound Service](https://developer.android.com/develop/background-work/services/bound-services#Messenger).


## Getting a token

The caller app sends an intent
```
val authIntent = Intent("com.ustadmobile.AUTH_GET_TOKEN", Uri.parse("local-auth://[server-url]")
```
server-url is optional. If server-url is omitted, then authenticator may prompt the user to select
a server where the Ustad app supports connecting to different servers.

* The Ustad app will allow the user to select an account, and will then ask the user if they wish
  to grant permission to the caller app.

* If the user accepts to grant permission to the caller app, the caller receives the result:

```
//In the form of username@https://server-url/
val accountName = resultIntent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)

//As per UstadAccountManager.ACCOUNT_TYPE
val accountType = resultIntent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)

//Auth token itself
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
        return Intent("com.ustadmobile.AUTH_GET_TOKEN", Uri.parse("local-auth://${input ?: ""}"))
    }

    override fun parseResult(resultCode: Int, intent: Intent?): GetTokenResult {
        val addedName = intent?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        val addedType = intent?.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
        val authToken = intent?.getStringExtra(AccountManager.KEY_AUTHTOKEN)

        return GetTokenResult(resultCode, addedName, addedType, authToken)
    }
}
```

Note: The Android AccountManager is not used here (as seems to be the case with all other third 
party login APIs) because the AccountManager does not allow the receiving app (e.g. the Ustad app) 
to receive the calling app's information e.g. callingActivity. This makes it impossible to tell the 
user which app is requesting permission.

The AccountManager does allow seamless sharing of authentication tokens by apps that share the same
signature, but that is not the use case here.

## Requesting user data using a token

The API works by sending serialized http requests using Android IPC.

