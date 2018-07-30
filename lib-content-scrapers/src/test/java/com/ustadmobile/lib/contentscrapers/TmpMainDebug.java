package com.ustadmobile.lib.contentscrapers;

import org.junit.Test;

public class TmpMainDebug {

    @Test
    public void testMain() {
        EdraakK12ContentScraper.main(new String[]{"https://programs.edraak.org/api/component/5a60a6663d99e104fb62c881/?states_program_id=41", "C:\\Users\\suhai\\cmdout\\"});
        EdraakK12ContentScraper.main(new String[]{"https://programs.edraak.org/api/component/5a60a5e1d1e59d0495fb8473/?states_program_id=41", "C:\\Users\\suhai\\cmdout2\\" });

    }

}
