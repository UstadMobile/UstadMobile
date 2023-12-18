Ustad Mobile server README

1) Open the ustad-server.conf file and set the siteUrl property to the url that
   will be used to access the site e.g. https://ustad.yourdomain.com/ (e.g. using a reverse proxy setup
   with Apache or Nginx in a production setup) or http://your.ip.address:8087/ (for testing/evaluation).

2) If you allow user self-registration (this can be enabled via settings after logging in as admin),
   you must configure the email section of the config file. The Children's Online Privacy Protection
   Act requires any app which may appeal to children to use an age-neutral selection screen, and if a
   user registering indicates that they are under 13, we must ask for a parental contact to grant
   consent (or deny) consent.

3) To start the server: use the .sh (Linux/MacOS) or .bat (Windows) start script in bin

A random admin password will be generated when the server runs for the first time: find it in
data/singleton/admin.txt

The configuration (ustad-server.conf) can be used to change the database (Postgres, SQLite),
listening port, and other options.

For further information on installing / configuration please see:

https://github.com/UstadMobile/UstadMobile/blob/dev-mvvm-primary/INSTALL.md



