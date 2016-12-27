cd ../../testres/
SPID=`cat DodgyHTTPD/dodgyhttpd.pid`
kill ${SPID}
if [ $? != 0 ]; then
    RUNNINGID=`ps -ef | grep "com.ustadmobile.dodgyhttpd.DodgyHTTPDServer" | grep " org.codehaus.plexus.classworlds.launcher.Launcher " | awk -F" " '{ print $2 }'`
    kill ${RUNNINGID}
    if [ $? != 0 ]; then
	echo "Unable to kill server - No server found to kill"
    fi
fi
