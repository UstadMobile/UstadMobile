package com.ustadmobile.lib.contentscrapers.khanacademy;

import com.ustadmobile.lib.contentscrapers.util.DownloadUrl;
import com.ustadmobile.lib.contentscrapers.util.SrtFormat;

import java.util.List;

class SubjectListResponse {

    public ComponentData componentProps;

    public class ComponentData {

        public Curation curation;

        public ItemResponse initialItem;

        public Card initialCards;

        public NavData tutorialNavData;

        public NavData tutorialPageData;

        public Transcript preloadedTranscript;

        public class Transcript {

            public String locale;

            public List<SrtFormat> subtitles;

        }

        public class Curation {

            public List<Tab> tabs;

            public class Tab {

                public List<ModuleResponse> modules;

            }
        }

        public class Card {

            public List<UserExercise> userExercises;

            public class UserExercise {

                public Model exerciseModel;

                public class Model {

                    public String id;

                    public String name;

                    public String dateModified;

                    public String kaUrl;

                    public String nodeSlug;

                    public List<Model> relatedContent;

                    public List<Model> relatedVideos;

                    public List<AssessmentItem> allAssessmentItems;

                    public class AssessmentItem {

                        public String id;

                        public boolean live;

                        public String sha;

                    }

                }


            }
        }

        public class NavData {

            public List<ContentModel> contentModels;

            public ContentModel contentModel;

            public List<ContentModel> navItems;

            public class ContentModel {

                public String id;

                public String nodeSlug;

                public String relativeUrl;

                public String slug;

                public String title;

                public String contentKind;

                public String kind;

                public String perseusContent;

                public DownloadUrl downloadUrls;

            }
        }
    }
}
