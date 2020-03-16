package com.ustadmobile.lib.contentscrapers.khanacademy

import com.ustadmobile.lib.contentscrapers.util.DownloadUrl
import com.ustadmobile.lib.contentscrapers.util.SrtFormat

class SubjectListResponse {

    var componentProps: ComponentData? = null

    inner class ComponentData {

        var curation: Curation? = null

        var initialItem: ItemResponse? = null

        var initialCards: Card? = null

        var tutorialNavData: NavData? = null

        var tutorialPageData: NavData? = null

        var preloadedTranscript: Transcript? = null

        inner class Transcript {

            var locale: String? = null

            var subtitles: List<SrtFormat>? = null

        }

        inner class Curation {

            var tabs: List<Tab>? = null

            inner class Tab {

                var modules: List<ModuleResponse>? = null

            }
        }

        inner class Card {

            var userExercises: List<UserExercise>? = null

            inner class UserExercise {

                var exerciseModel: Model? = null

                inner class Model {

                    var id: String? = null

                    var name: String? = null

                    var dateModified: String? = null

                    var kaUrl: String? = null

                    var nodeSlug: String? = null

                    var relatedContent: List<Model>? = null

                    var relatedVideos: List<Model>? = null

                    var allAssessmentItems: List<AssessmentItem>? = null

                    inner class AssessmentItem {

                        var id: String? = null

                        var live: Boolean = false

                        var sha: String? = null

                    }

                }


            }
        }

        inner class NavData {

            var contentModels: MutableList<ContentModel>? = null

            var contentModel: ContentModel? = null

            var navItems: List<ContentModel>? = null

            inner class ContentModel {

                var id: String? = null

                var nodeSlug: String? = null

                var relativeUrl: String? = null

                var youtubeId: String? = null

                var slug: String? = null

                var title: String? = null

                var contentKind: String? = null

                var kind: String? = null

                var perseusContent: String? = null

                var downloadUrls: DownloadUrl? = null

                var dateModified: String? = null

                var creationDate: String? = null

                var allAssessmentItems: List<Card.UserExercise.Model.AssessmentItem>? = null

                var relatedContent: List<String>? = null

            }
        }
    }
}
