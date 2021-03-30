package com.ustadmobile.sharedse.network.containerfetcher

import java.net.HttpURLConnection
import java.net.URL


typealias ConnectionOpener = (url: URL) -> HttpURLConnection

