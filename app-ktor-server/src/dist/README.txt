Ustad Mobile server README

1) Install the required media helper programs

   On Ubuntu (23.10+):
   apt-get install openjdk-17-jdk mediainfo vlc sox libsox-fmt-all handbrake-cli
   Optional: apt-get install ghostscript (provides PDF compression)

   On Ubuntu(23.04 and earlier)
   apt-get install openjdk-17-jdk mediainfo vlc sox libsox-fmt-all flatpak
   Install the latest HandBrake CLI using flatpak as per https://handbrake.fr/downloads2.php
   flatpak install /path/where/downloaded/HandBrakeCLI-1.7.3-x86_64.flatpak
   Optional: apt-get install ghostscript (provides PDF compression)

   On Windows:
   winget install -e --id MediaArea.MediaInfo
   winget install -e --id HandBrake.HandBrake.CLI
   winget install -e --id HandBrake.HandBrake.CLI
   Download and install sox from: https://sourceforge.net/projects/sox/files/sox/14.4.2/
   Download and unzip mpg123 from https://www.mpg123.de/download/win32/ and unzip into the commands directory
   winget install -e --id ArtifexSoftware.GhostScript (optional, provides PDF compressino)

2) Open the ustad-server.conf file and set the siteUrl property to the url that
   will be used to access the site e.g. https://ustad.yourdomain.com/ (e.g. using a reverse proxy setup
   with Apache or Nginx in a production setup) or http://your.ip.address:8087/ (for testing/evaluation).

3) If you allow user self-registration (this can be enabled via settings after logging in as admin),
   you must configure the email section of the config file. The Children's Online Privacy Protection
   Act requires any app which may appeal to children to use an age-neutral selection screen, and if a
   user registering indicates that they are under 13, we must ask for a parental contact to grant
   consent (or deny) consent.

4) To start the server: use the .sh (Linux/MacOS) or .bat (Windows) start script in bin

A random admin password will be generated when the server runs for the first time: find it in
data/singleton/admin.txt

The configuration (ustad-server.conf) can be used to change the database (Postgres, SQLite),
listening port, and other options.

For further information on installing / configuration please see:

https://github.com/UstadMobile/UstadMobile/blob/primary/INSTALL.md

Start on system boot:

This should be done using SystemD on most Linux distributions (including Ubuntu). Modify the paths
and user (if needed) in systemd/ustad-server.service and then install the service:

cp unzip-path/systemd/ustad-server.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl start ustad-server

#Check status
sudo systemctl status ustad-server
