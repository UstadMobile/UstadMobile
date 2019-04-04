package com.ustadmobile.lib.contentscrapers.khanacademy;

import java.util.List;

public class TopicListResponse {

    public ComponentData componentProps;

    public class ComponentData {

        public List<Modules> modules;

        public class Modules {

            public List<Domains> domains;

            public class Domains {

                public String href;

                public String icon;

                public String translatedTitle;

                public String identifier;

                public List<Domains> children;

            }
        }
    }
}
