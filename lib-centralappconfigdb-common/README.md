# Lib-CentralAppConfigDb

The central app config db module handles app wide settings. In the ideal world there would be no need 
for  such a database, any client could simply connect to any server. In reality the need to register 
verified app links on Android for the app to open deep links and use passkeys makes some amount of
linking the app to particular domains at compile time unavoidable.

Having a central URL makes it possible to provide a list of known learning spaces such that the user
need not type them every time.

* Configuration
  * Primary URL (used for passkeys, applink verification)
  * The Learning Space URL to use for creation of new personal accounts, if any
  * A preset Learning Space URL to use on systems where there is only a single learning space. If 
    this is set (e.g. non-null), the user will not be asked to or able to select any other learning space.
* List of available Learning Spaces on the system
* Configuration data for each Learning Space e.g. its URL, the database JDBC URL, username, 
  and password.

Bug (soon to be resolved) for SQLdelight in Android Studio:
https://github.com/sqldelight/sqldelight/issues/5310
