package com.ustadmobile.lib.contentscrapers.khanacademy;

import java.util.ArrayList;
import java.util.Map;

public class ItemData {

    public Content question;

    public ArrayList<Content> hints;

    public class Content {

        public String content;

        public Map<String, Image> images;

        public class Image {

            public int width;

            public int height;

        }

        public Map<String, Widget> widgets;

        public class Widget {

            public Options options;

            public class Options{

                public ArrayList<Option> options;

                public class Option {

                    String content;

                }

            }

        }

    }
}
