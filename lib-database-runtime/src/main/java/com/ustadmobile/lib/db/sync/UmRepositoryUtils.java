package com.ustadmobile.lib.db.sync;

import java.util.List;

public class UmRepositoryUtils {

    public static UmRepositoryDb findRepository(String baseUrl, String auth, Object db,
                                                List<UmRepositoryDb> repositoryDbList) {
        for(UmRepositoryDb repo : repositoryDbList) {
            if(repo.getBaseUrl().equals(baseUrl) && repo.getAuth().equals(auth)
                    && repo.getDatabase().equals(db))
                return repo;
        }

        return null;
    }

}
