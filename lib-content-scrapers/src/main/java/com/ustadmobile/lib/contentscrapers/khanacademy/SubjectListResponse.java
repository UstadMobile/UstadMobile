package com.ustadmobile.lib.contentscrapers.khanacademy;

import java.util.List;

class SubjectListResponse {

    public ComponentData componentProps;

    public class ComponentData {

        public Curation curation;

        public class Curation {

            public List<Tab> tabs;

            public class Tab {

                public List<ModuleResponse> modules;

            }
        }
    }
}
