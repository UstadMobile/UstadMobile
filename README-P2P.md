# Peer to peer content sharing.

We have used Wi-Fi Direct and Bluetooth technologies to achieving peer to peer 
content sharing.Apart from that we have used Network Service Discovery to help our app
making reasonable decisions.

## Wi-Fi Direct

This technology allows Android 4.0 (API level 14) and later Android version devices 
with the appropriate hardware to connect directly to one another via Wi-Fi without 
an intermediate access point. Using Wi-Fi Direct we were able to discover and connect 
to other devices which have the UstadMobile app installed.

### How we used it.

We have two different scenarios of which Wi-Fi Direct technology was used to foster peer 
to peer content sharing.

#### 1. Supernode Mode

This is a mode which allows UstadMobile app installed in your device to start broadcasting 
a Wi-Fi Direct service.This service simply notifies other users who have UstadMobile app 
installed that your device can offer material if someone is in need by just giving all necessary device addresses. 
The user can enter this mode by just flipping the switch on Data settings activity.

#### 2. Client Mode

When UstadMobile app is in client mode which can be achieved also by just flipping the switch on 
Data Settings section, our app will be able to use Wi-Fi Direct to scan for all services broadcasted 
from supernode apps.<br/>
We have used Network service discovery to update few information found from Wi-Fi 
Direct service of which will be used to decide whether the file can be found locally (From peer device) or not.

## Bluetooth

This technology which allows a device to wirelessly exchange data with other Bluetooth devices.

### How we used these technologies

When a user needs a course, our app will decide where to get it by checking if it can be downloaded locally (From peer device) 
or from the cloud.
Our app will use information found from Wi-Fi direct service Bluetooth address being one of them to connect 
to that device via Bluetooth and ask if the file that user wants has already been downloaded on that peer device.Peer device 
will reply with either YES is can be downloaded from it or NO.

#### If NO

The file will be downloaded from the cloud, in this case HTTP connection will be made and file will be downloaded.

#### If YES.

The device in client mode via Bluetooth sends a request to download that file, once peer device on supernode receive 
this request it creates Wi-Fi Direct group and sends Wi-Fi Group SSID and Passphrase back to the device which requested to download a file.
If Group has already been created, it will just send Group SSID and Passphrase without recreating a group.<br/>

Upon receiving the Group SSID and Passphrase, Bluetooth connection will be terminated and it will connect to that group.Once a connection is made,
file download task will be started.After acquiring the file, it will disconnect from the group to preserve your device power since sometimes Wi-Fi use a lot of power in its operations.
We have experienced this on Samsung devices, it will warn you about this.




