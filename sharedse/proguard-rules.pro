#
# BleProxyResponder requires reflection based access to the HTTPSession headers field so it can
# be serialized and sent over BLE.
#
-keepclassmembernames class fi.iki.elonen.NanoHTTPD$HTTPSession {
java.util.Map headers; 
}

-keep class com.ustadmobile.sharedse.network.ContainerDownloadManagerImpl$MutableLiveDataWithRef {
    ** reference;
}
