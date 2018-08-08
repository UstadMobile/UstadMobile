package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public interface ContentScraper {

    ArrayList<OpdsEntryWithRelations> convert(String url, File destinationDir) throws IOException;

}
