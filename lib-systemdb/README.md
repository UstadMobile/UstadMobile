# Lib-SystemDb

The system db includes system-wide settings and data:

* Configuration
  * Primary URL (used for passkeys, applink verification)
  * The Learning Space URL to use for creation of new personal accounts, if any
  * A preset Learning Space URL to use on systems where there is only a single learning space. If 
    this is set (e.g. non-null), the user will not be asked to or able to select any other learning space.
* List of available Learning Spaces on the system
* Configuration data for each Learning Space e.g. the database JDBC URL, username, and password.
