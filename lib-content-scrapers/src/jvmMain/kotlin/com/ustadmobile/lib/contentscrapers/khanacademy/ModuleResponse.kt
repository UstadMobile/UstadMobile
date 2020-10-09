package com.ustadmobile.lib.contentscrapers.khanacademy

import com.ustadmobile.lib.contentscrapers.util.DownloadUrl

class ModuleResponse {

    var description: String? = null

    var icon: String? = null

    var kind: String? = null

    var slug: String? = null

    var title: String? = null

    var topicId: String? = null

    var imageUrl: String? = null

    var url: String? = null

    var tutorials: List<Tutorial>? = null

    var modules: List<ModuleResponse>? = null

    var contentItems: List<Tutorial.ContentItem>? = null

    var subjectChallenge: SubjectChallenge? = null

    inner class Tutorial {

        var contentItems: List<ContentItem>? = null

        var description: String? = null

        var id: String? = null

        var slug: String? = null

        var title: String? = null

        var url: String? = null

        inner class ContentItem {

            var contentId: String? = null

            var description: String? = null

            var kind: String? = null

            var thumbnailUrl: String? = null

            var slug: String? = null

            var nodeUrl: String? = null

            var title: String? = null

            var expectedDoNCount: Int = 0

            var downloadUrls: DownloadUrl? = null

        }
    }

    inner class SubjectChallenge// TODO

}
