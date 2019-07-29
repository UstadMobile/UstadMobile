package com.ustadmobile.lib.contentscrapers.khanacademy

class TopicListResponse {

    var componentProps: ComponentData? = null

    inner class ComponentData {

        var modules: List<Modules>? = null

        inner class Modules {

            var domains: List<Domains>? = null

            inner class Domains {

                var href: String? = null

                var icon: String? = null

                var translatedTitle: String? = null

                var identifier: String? = null

                var children: List<Domains>? = null

            }
        }
    }
}
