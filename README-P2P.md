# Peer to peer content sharing.

We have used Wi-Fi Direct and Bluetooth technologies to achieve peer to peer 
content sharing.Apart from that we have used Network Service Discovery to help our app
make reasonable decisions.

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
installed that your device can offer course material if someone is in need by just giving all 
necessary device addresses like device IP address and Bluetooth Address. 
The user can enter this mode by just flipping the switch on Data settings activity.

#### 2. Client Mode

When UstadMobile app is in client mode which can be achieved also by just flipping the switch on 
Data Settings section, our app will be able to use Wi-Fi Direct to scan for all services broadcasted 
from supernode apps.<br/>
We have used Network service discovery to update few information found from Wi-Fi 
Direct service of which will be used to decide whether the file can be found locally (From peer device) or not.

## Bluetooth

This ia a technology which allows a device to wirelessly exchange data with other Bluetooth devices.

### How we used these technologies

When a user needs a course, our app will decide where to get it by checking if it can be downloaded locally (From peer device) 
or from the cloud.
Our app will use information found from Wi-Fi direct service Bluetooth address being one of them to connect 
to that device via Bluetooth and ask if the file that user wants has already been downloaded on that peer device.Peer device 
will reply with either YES is can be downloaded from it or NO.

#### If NO

Having this as a feedback from peer device it means file will be downloaded from the cloud.<br/>
In this case your device will initiate HTTP connection and download the file from the cloud.

#### If YES.
Having this as a feedback from peer devices it means file can be downloaded locally but we don't know if peer device 
and your device are on the same network or not, at this point we use Network Service discovery information 
(Which is time passed from the last time that peer device information was updated on your device) to decide 
how to download a file from the following options:-<br/>
* <b>Peer device has a file that you are looking for, but it is connected on a different network.</b><br/><br/>
When peer device is on a different network with your device, your device via Bluetooth sends a request to download 
that file, once peer device receives this request it creates Wi-Fi Direct group and sends Wi-Fi Group SSID and 
Passphrase back to the device which requested to download a file.
If Group has already been created, it will just send Group SSID and Passphrase without recreating a group.<br/><br/>
Upon receiving the Group SSID and Passphrase, Bluetooth connection will be terminated and it will connect to that group.Once a connection is made,
file download task will be started.
After acquiring the file, it will disconnect from the group to preserve your device power since sometimes Wi-Fi Direct use a lot of power in its operations.<br/><br/>
<b>Samsung devices will tend to give you warning and advice you to switch off Wi-Fi Direct after using it.</b>

* <b>Peer device has a file you are looking for and it is on the same network with your device.</b><br/><br/>
In this case, your device will initiate HTTP connection and download a file from a peer device.





