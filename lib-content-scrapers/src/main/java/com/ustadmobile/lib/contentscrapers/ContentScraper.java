package com.ustadmobile.lib.contentscrapers;

import java.io.File;
import java.io.IOException;

public interface ContentScraper {

    void convert(String url, File destinationDir) throws IOException;



}
